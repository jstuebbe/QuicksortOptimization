package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.wwu.Quicksort.SortingStep;
import de.wwu.Quicksort.utils.Classifier;
import de.wwu.Quicksort.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Pair;

/**
 * Implementation des Sortierverfahrens: IPS4o. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * Diese Klasse enthält original Kommentare aus der Implementation von Sascha Witt (https://github.com/SaschaWitt/ips4o)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class IPS4oSorter<I extends Comparable<I>> extends TexStringGenerator<I> {
	final int BASECASESIZE = 4; //1024
	final int MAXBUCKETS = 4;
	final int SAMPLESIZE = MAXBUCKETS - 1;
	final int PUFFERSIZE = 3;
	//TODO
	final int SINGLELEVELTHRESHOLD = 1;
	final int OVERSAMPLINGFACTOR = 1;

	private Classifier<I> classifier;

	private ArrayList<ArrayList<I>> puffers;
	private ArrayList<ArrayList<I>> swapPuffers;
	private ArrayList<I> overflowPuffer;
	private int[][] bucketPointers;
	private int[] bucketStart;
	private int[] pufferIndices;
	private int[] bktsize;
	private int numBuckets; //ist bei uns gleich MAXBUCKETS = BUCKETCOUNT

	public enum IPS4o_PHASE 
	{
		CONSTANTS(0),
		BUILD_TREE(1),
		LOCAL_CLASSIFICATION(2),
		LOCAL_CLASSIFICATION_FULL_BUFFER(3),
		BLOCK_PERMUTATION_POINTERS(4),
		BLOCK_PERMUTATION_READ_FROM_PRIMARY_BUCKET(5),
		BLOCK_PERMUTATION_NEW_BUCKET(6),
		BLOCK_PERMUTATION_LOAD_IN(7),
		BLOCK_PERMUTATION_WRITE_IN(8),
		CLEANUP(9);

		private final int value;
		private IPS4o_PHASE(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
	}

	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}

		Map<String,Integer> constantsMap = new HashMap<String,Integer>();
		constantsMap.put("BASECASESIZE", BASECASESIZE);
		constantsMap.put("SAMPLESIZE", SAMPLESIZE);
		constantsMap.put("MAXBUCKETS", MAXBUCKETS);
		constantsMap.put("OVERSAMPLINGFACTOR", OVERSAMPLINGFACTOR);
		constantsMap.put("PUFFERSIZE", PUFFERSIZE);
		constantsMap.put("SINGLELEVELTHRESHOLD", SINGLELEVELTHRESHOLD);
		saveSortingStep(sortingSteps, input, 0, input.size()-1, null, null, null, null, null, null,null,
				constantsMap, null, null, null, null, null, null, null, null, IPS4o_PHASE.CONSTANTS, null);
		sequential(input,0,input.size() - 1, sortingSteps);
	}

	public void sequential(ArrayList<I> input, int l, int r, List<SortingStep<I>> sortingSteps) {
		int n = r - l + 1; // r - l + 1 glaube ich, r - l  original
		if(n <= 2 * BASECASESIZE) {
			Utils.smallSort(input,l,r);
			return;
		}
		//Das sind die d_i mit dem letzten d als Ende
		bucketStart = new int[MAXBUCKETS+1]; 

		//Partitioning
		//------------------------------------------------

		Pair<Integer,Boolean> res = partition(input, l,r,sortingSteps);
		numBuckets = res.getKey();
		boolean equalBuckets = res.getValue();

		// Final base case is executed in cleanup step, so we're done here
		if (n <= SINGLELEVELTHRESHOLD) {
			return;
		}

		// Recurse
		int[] localBucketStart = bucketStart;
		int localNumBuckets = numBuckets;
		for (int i = 0; i < localNumBuckets; i += equalBuckets ? 2 : 1) {
			int start = localBucketStart[i];
			int stop = localBucketStart[i + 1] - 1;
			if (stop - start > 2 * BASECASESIZE)
				sequential(input,l + start, l + stop,sortingSteps);
		}
		if (equalBuckets) {
			int start = localBucketStart[localNumBuckets - 1];
			int stop = localBucketStart[localNumBuckets];
			if (stop - start > 2 * BASECASESIZE)
				sequential(input,l + start, l + stop - 1,sortingSteps);
		}
	}

	public Pair<Integer,Boolean> partition(ArrayList<I> input, int begin, int end,List<SortingStep<I>> sortingSteps) {
		// Sampling
		boolean useEqualBuckets = false;
		//std::tie(numBuckets, useEqualBuckets) = buildClassifier(begin, end, local_.classifier);
		//was bedeutet useEqualsBuckets? Nehme hier nein an. Benutze 
		classifier = new Classifier<I>(SAMPLESIZE,MAXBUCKETS);
		Pair<Integer,Boolean> res = classifier.build(input,begin,end);
		saveSortingStep(sortingSteps, input, begin, end, null, null, null, null, null, null, null,
				null,null, null, null, null, classifier.getSamples(), classifier.getSplitters(), null, null, IPS4o_PHASE.BUILD_TREE, MAXBUCKETS);
		numBuckets = res.getKey();
		useEqualBuckets = res.getValue();

		puffers = new ArrayList<ArrayList<I>>();
		for(int z = 0; z < MAXBUCKETS; z++) {
			puffers.add(new ArrayList<I>((Collections.nCopies(PUFFERSIZE, null))));
		}
		pufferIndices = new int[MAXBUCKETS]; 
		bktsize = new int[MAXBUCKETS];
		sequentialClassification(input,begin,end,useEqualBuckets,sortingSteps);

		// Compute which bucket can cause overflow
		int overflowBucket = computeOverflowBucket();
		swapPuffers = new ArrayList<ArrayList<I>>();
		swapPuffers.add(new ArrayList<I>((Collections.nCopies(PUFFERSIZE, null))));
		swapPuffers.add(new ArrayList<I>((Collections.nCopies(PUFFERSIZE, null))));

		// Block Permutation
		permuteBlocks(input,begin,end,useEqualBuckets,sortingSteps);

		// Cleanup
		// Save excess elements at right end of stripe
		Pair<Integer,Integer> inSwapPuffer = new Pair<Integer, Integer>(-1, 0);

		// Write remaining elements
		writeMargins(input,begin,end,0, numBuckets, overflowBucket, inSwapPuffer.getKey(), inSwapPuffer.getValue(),sortingSteps);

		return new Pair<Integer,Boolean>(numBuckets,useEqualBuckets);
	}

	public void writeMargins(ArrayList<I> input, int begin, int end, int firstBucket, int lastBucket,
			int overflowBucket, int swapBucket,int inSwapPuffer,List<SortingStep<I>> sortingSteps){
		boolean isLastLevel = end - begin <= SINGLELEVELTHRESHOLD;
		//		    int comp = classifier_->getComparator();

		for (int i = firstBucket; i < lastBucket; ++i) {

			// Get bucket information
			int bstart = bucketStart[i];
			int bend = bucketStart[i + 1];
			int bwrite = bucketPointers[i][0];
			// Destination where elements can be written
			int dst = begin + bstart;
			int remaining = Utils.alignToNextBlock(bstart, PUFFERSIZE) - bstart;

			if (i == overflowBucket && overflowPuffer != null) {
				// Is there overflow?

				int tailSize = PUFFERSIZE - remaining;

				// Fill head
				for(int j = 0; j < remaining; j++) {
					input.set(dst + j, overflowPuffer.remove(0));
				}

				//				TODO
				remaining = Integer.MAX_VALUE;

				// Write remaining elements into tail
				dst = begin + (bwrite - PUFFERSIZE);
				for(int j = 0; j < tailSize; j++) {
					input.set(dst + j, overflowPuffer.remove(0));
				}
				dst = dst + tailSize;
				overflowPuffer = null;

			} 
			else if (bwrite > bend && bend - bstart > PUFFERSIZE) {



				int headSize = bwrite - bend;

				// Write to head
				//				int src = begin + bend;
				for(int j = 0; j < headSize; j++) {
					input.set(dst+j, input.get(begin+bend+j));
				}
				dst = dst + headSize;
				//				dst = std::move(src, src + head_size, dst);
				remaining -= headSize;
			}

			// Write elements from buffers
			ArrayList<I> src = puffers.get(i);
			int count = pufferIndices[i];
			if (count <= remaining) {
				for(int j = 0; j < count; j++) {
					input.set(dst+j, src.get(j));
				}
				dst = dst + count;
				remaining -= count;
			} else {
				for(int j = 0; j < remaining; j++) {
					input.set(dst+j, src.get(j));	
				}
				count -= remaining;
				dst = begin + bwrite;
				for(int j = 0; j < count;j++) {
					input.set(dst+j, src.get(remaining+j));
				}
				dst = dst + count;
			}

			puffers.set(i, new ArrayList<I>((Collections.nCopies(PUFFERSIZE, null))));

			// Perform final base case sort here, while the data is still cached
			saveSortingStep(sortingSteps, input, begin, end, null, null, null ,null, null, null, null, null,
					bucketStart, null, null, null, null, null, puffers, swapPuffers, IPS4o_PHASE.CLEANUP, MAXBUCKETS);	
			if (isLastLevel || (bend-1 - bstart) <= 2 * BASECASESIZE) Utils.smallSort(input, begin+bstart, begin+bend-1);
			saveSortingStep(sortingSteps, input, begin, end, null, null, null, null, null, null, null, null,
					bucketStart, null, null, null, null, null, puffers, swapPuffers, IPS4o_PHASE.CLEANUP, MAXBUCKETS);
		}
	}

	public void permuteBlocks(ArrayList<I> input,int begin, int end,boolean useEqualBuckets,List<SortingStep<I>> sortingSteps) {
		int readBucket = 0;
		// Not allowed to write to this offset, to avoid overflow
		int maxOff = Utils.alignToNextBlock(end + 1 - begin + 1, PUFFERSIZE) - PUFFERSIZE;

		// Go through all buckets
		for (int count = numBuckets; count != 0; --count) {
			int destBucket;
			// Try to read a block ...
			while ((destBucket = classifyAndReadBlock(input,begin,readBucket, useEqualBuckets,sortingSteps)) != -1) {
				int currentSwap = 0;
				// ... then write it to the correct bucket
				while ((destBucket = swapBlock(input, begin,maxOff, destBucket, currentSwap, useEqualBuckets,sortingSteps)) != -1) {
					// Read another block, keep going
					currentSwap = (currentSwap + 1) % 2;
				}
			}
			readBucket = (readBucket + 1) % numBuckets;
		}
	}

	public int classifyAndReadBlock(ArrayList<I> input, int begin,int readBucket, boolean useEqualBuckets,List<SortingStep<I>> sortingSteps) {
		int[] bucketPointer = bucketPointers[readBucket];
		int write, read;
		write = bucketPointer[0];
		read = bucketPointer[1];
		bucketPointer[1] -= SAMPLESIZE;
		saveSortingStep(sortingSteps, input, begin, null, null, null, null, readBucket, null, null, null, null,
				bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.BLOCK_PERMUTATION_POINTERS, MAXBUCKETS);
		if (read < write) {
			// No more blocks in this bucket
			return -1;
		}

		// Read block
		for(int i = 0; i < PUFFERSIZE; i++) {
			swapPuffers.get(0).set(i,input.get(i+begin+read));
		}
		saveSortingStep(sortingSteps, input, begin, null, null, null, null, readBucket, read, null, null, null,
				bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers,
				IPS4o_PHASE.BLOCK_PERMUTATION_READ_FROM_PRIMARY_BUCKET, MAXBUCKETS);

		return classifier.classify(swapPuffers.get(0).get(0));
	}

	public int swapBlock(ArrayList<I> input, int begin, int maxOff, int destBucket, int currentSwap, boolean useEqualBuckets,List<SortingStep<I>> sortingSteps) {
		int write, read;
		int newDestBucket;
		int[] bucketPointer = bucketPointers[destBucket];
		do {
			write = bucketPointer[0];
			read = bucketPointer[1];
			bucketPointer[0] += PUFFERSIZE;
			saveSortingStep(sortingSteps, input, begin, null, maxOff, destBucket, currentSwap, null, write, null, null, null,
					bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.BLOCK_PERMUTATION_POINTERS, MAXBUCKETS);
			if (write > read) {
				// Destination block is empty
				if (write >= maxOff) {
					// Out-of-bounds; write to overflow buffer instead
					overflowPuffer = new ArrayList<I>();
					for(int i = 0; i < PUFFERSIZE; i++) {
						overflowPuffer.add(swapPuffers.get(currentSwap).get(i));
						//für bessere didaktische Darstellung
						swapPuffers.get(currentSwap).set(i, null);
					}
					saveSortingStep(sortingSteps, input, begin, null, maxOff, destBucket, currentSwap, null, write, null, null, null,
							bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.BLOCK_PERMUTATION_WRITE_IN, MAXBUCKETS);
					return -1;
				}
				// Write block
				for(int i = 0; i < SAMPLESIZE; i++) {
					input.set(i + begin + write, swapPuffers.get(currentSwap).get(i));
				}
				saveSortingStep(sortingSteps, input, begin, null, maxOff, destBucket, currentSwap, null, write, null, null, null,
						bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.BLOCK_PERMUTATION_WRITE_IN, MAXBUCKETS);
				return -1;	
			}
			// Check if block needs to be moved
			newDestBucket = classifier.classify(input.get(begin + write));
			saveSortingStep(sortingSteps, input, begin, null, maxOff, destBucket, currentSwap, null, write, null, newDestBucket, null,
					bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.BLOCK_PERMUTATION_NEW_BUCKET, MAXBUCKETS);
		} while (newDestBucket == destBucket);

		// Swap blocks
		int notCurrentSwap = (currentSwap + 1) % 2;
		for(int i = 0; i < SAMPLESIZE; i++) {
			swapPuffers.get(notCurrentSwap).set(i,input.get(begin+write+i));
		}
		saveSortingStep(sortingSteps, input, begin, null, maxOff, destBucket, currentSwap, null, write, null, null, null,
				bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.BLOCK_PERMUTATION_LOAD_IN, MAXBUCKETS);
		for(int i = 0; i < SAMPLESIZE; i++) {
			input.set(begin+write+i, swapPuffers.get(currentSwap).get(i));
		}
		saveSortingStep(sortingSteps, input, begin, null, maxOff, destBucket, currentSwap, null, write, null, null, null,
				bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.BLOCK_PERMUTATION_WRITE_IN, MAXBUCKETS);
		return newDestBucket;
	}

	public int computeOverflowBucket() {
		int bucket = numBuckets - 1;
		while (bucket >= 0 && (bucketStart[bucket + 1] - bucketStart[bucket]) <= PUFFERSIZE)
			--bucket;
		return bucket;
	}

	//Local classification in the sequential case. 
	public void sequentialClassification(ArrayList<I> input, int begin, int end,boolean useEqualBuckets,List<SortingStep<I>> sortingSteps) {
		int myFirstEmptyBlock = classifyLocally(input,begin, end,useEqualBuckets,sortingSteps);

		int sum = 0;
		bucketStart[0] = 0;
		for (int i = 0; i < numBuckets; ++i) {
			sum += bktsize[i];
			bucketStart[i + 1] = sum;
		}

		bucketPointers = new int[numBuckets][2];
		// Set write/read pointers for all buckets
		for (int bucket = 0; bucket < numBuckets; ++bucket) {
			int start = Utils.alignToNextBlock(bucketStart[bucket], PUFFERSIZE);
			int stop = Utils.alignToNextBlock(bucketStart[bucket + 1], PUFFERSIZE);
			bucketPointers[bucket][0] = start;
			bucketPointers[bucket][1] =  (start >= myFirstEmptyBlock ? start : (stop <= myFirstEmptyBlock ? stop : myFirstEmptyBlock)) - PUFFERSIZE; 
		}
		saveSortingStep(sortingSteps, input, begin, end, null, null, null, null, null, null, null, null,
				bucketStart, bktsize, pufferIndices, bucketPointers, null, null, puffers, swapPuffers, IPS4o_PHASE.LOCAL_CLASSIFICATION, MAXBUCKETS);
	}

	public int classifyLocally(ArrayList<I> input, int begin, int end, boolean useEqualBuckets,List<SortingStep<I>> sortingSteps) {
		int write = begin;
		for(int i = begin; i < end + 1; i++) {
			int bucket = classifier.classify(input.get(i));
			if(pufferIndices[bucket] == PUFFERSIZE) {
				saveSortingStep(sortingSteps, input, begin, end, null, null, null, null, write, i, bucket, null,
						null,bktsize, pufferIndices, null, null, null, puffers, swapPuffers, IPS4o_PHASE.LOCAL_CLASSIFICATION_FULL_BUFFER,MAXBUCKETS);
				for(int j = 0; j < PUFFERSIZE;j++) {
					input.set(write+j, puffers.get(bucket).get(j));
					//für bessere Didaktische Darstellung
					puffers.get(bucket).set(j, null);
				}
				write += PUFFERSIZE;
				bktsize[bucket] += PUFFERSIZE;
				pufferIndices[bucket] = 0;
				saveSortingStep(sortingSteps, input, begin, end, null, null, null, null, write, i, bucket, null,
						null,bktsize, pufferIndices, null, null, null, puffers, swapPuffers, IPS4o_PHASE.LOCAL_CLASSIFICATION,MAXBUCKETS);
			}
			puffers.get(bucket).set(pufferIndices[bucket]++, input.get(i));
			saveSortingStep(sortingSteps, input, begin, end, null, null, null, null, write, i, bucket, null,
					null,bktsize, pufferIndices, null, null, null, puffers, swapPuffers, IPS4o_PHASE.LOCAL_CLASSIFICATION,MAXBUCKETS);
		}

		// Update bucket sizes to account for partially filled buckets
		for (int i = 0; i < numBuckets; ++i) bktsize[i] += pufferIndices[i];

		return write - begin;
	}

	public static <I extends Comparable<I>> void saveSortingStep(List<SortingStep<I>> sortingSteps,ArrayList<I> input, Integer l, Integer r,
			Integer maxOff, Integer destBucket, Integer currentSwap,Integer readBucket, Integer current , Integer element, Integer bucket,
			Map<String,Integer> constantsMap, int[] bucketStart, int[] bktsize, int[] pufferIndices, int[][] bucketPointers, ArrayList<I> samples,
			ArrayList<I> splitters, ArrayList<ArrayList<I>> puffers, ArrayList<ArrayList<I>> swapPuffers, IPS4o_PHASE phase, Integer MAXBUCKETS) {
		SortingStep<I> step = new SortingStep<I>();

		if(constantsMap != null) {
			if(constantsMap.get("BASECASESIZE") != null) step.addToIndexMap("BASECASESIZE", constantsMap.get("BASECASESIZE"));
			if(constantsMap.get("SAMPLESIZE") != null) step.addToIndexMap("SAMPLESIZE",  constantsMap.get("SAMPLESIZE"));
			if(constantsMap.get("MAXBUCKETS") != null) step.addToIndexMap("MAXBUCKETS",  constantsMap.get("MAXBUCKETS"));
			if(constantsMap.get("OVERSAMPLINGFACTOR") != null) step.addToIndexMap("OVERSAMPLINGFACTOR",  constantsMap.get("OVERSAMPLING_FACTOR"));
			if(constantsMap.get("PUFFERSIZE") != null) step.addToIndexMap("PUFFERSIZE",  constantsMap.get("PUFFERSIZE"));
			if(constantsMap.get("SINGLELEVELTHRESHOLD") != null) step.addToIndexMap("SINGLELEVELTHRESHOLD",  constantsMap.get("SINGLELEVELTHRESHOLD"));
		}

		if(input != null) step.addToArrayListMap("input", new ArrayList<I>(input));
		if(l != null) step.addToIndexMap("l", l);
		if(r != null) step.addToIndexMap("r", r);
		if(current != null) step.addToIndexMap("current", current);
		if(bucket != null) step.addToIndexMap("bucket", bucket);
		if(maxOff != null) step.addToIndexMap("maxOff", maxOff);	
		if(destBucket != null) step.addToIndexMap("destBucket", destBucket);
		if(currentSwap != null) step.addToIndexMap("currentSwap", currentSwap);
		if(phase != null) step.addToIndexMap("phase", phase.getValue());
		if(readBucket != null) step.addToIndexMap("readBucket", readBucket);
		if(element != null) step.addToIndexMap("element", element);

		if(bucketPointers != null) {
			for(int z = 0; z < bucketPointers.length; z++) {
				step.addToIndexArrayMap("bucketPointer" + z, Arrays.stream( bucketPointers[z] ).boxed().toArray( Integer[]::new ));
			}
		}

		if(puffers != null && MAXBUCKETS != null) {
			for(int z = 0; z < MAXBUCKETS; z++) {
				step.addToArrayListMap("puffer" + z, new ArrayList<I>(puffers.get(z)));
			}
		}
		if(samples != null) step.addToArrayListMap("samples", new ArrayList<I>(samples));	
		if(splitters != null) step.addToArrayListMap("splitters", new ArrayList<I>(splitters));	

		if(swapPuffers != null) {
			for(int z = 0; z < 2; z++) {
				step.addToArrayListMap("swapPuffer" + z, new ArrayList<I>(swapPuffers.get(z)));
			}
		}

		if(pufferIndices != null) step.addToIndexArrayMap("pufferIndices", Arrays.stream( pufferIndices ).boxed().toArray( Integer[]::new ));
		if(bktsize != null) step.addToIndexArrayMap("bktsize", Arrays.stream( bktsize ).boxed().toArray( Integer[]::new ));
		if(bucketStart != null) step.addToIndexArrayMap("bucketStart", Arrays.stream( bucketStart ).boxed().toArray( Integer[]::new ));

		sortingSteps.add(step);
	}

	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		int legendCounter = 0;
		String texString = getTexIntro();
		String[] input;		
		//String[] samples; // Kann in einer Erweiterung hinzugefügt werden.
		String[] splitters;
		//Integer[] pufferIndices; // Kann in einer Erweiterung hinzugefügt werden.
		Integer[] bktsize;
		Integer[] bucketStart;

		ArrayList<String[]> puffers;
		Integer[][] bucketPointers;
		ArrayList<String[]> swapPuffers;

		Integer l;
		Integer r;
		Integer current;
		Integer bucket;
		Integer readBucket;
		//Integer maxOff; // Kann in einer Erweiterung hinzugefügt werden.
		Integer destBucket;
		Integer currentSwap;
		Integer element;

		IPS4o_PHASE phase;
		IPS4o_PHASE currentPhase = null;

		SortingStep<I> firstStep = sortingSteps.get(0);
		int size = firstStep.getFromArrayListMap("input").size();
		final Integer BASECASESIZE = firstStep.getFromIndexMap("BASECASESIZE") != null ? firstStep.getFromIndexMap("BASECASESIZE") : null;
		final Integer SAMPLESIZE = firstStep.getFromIndexMap("SAMPLESIZE") != null ? firstStep.getFromIndexMap("SAMPLESIZE") : null;
		final Integer MAXBUCKETS = firstStep.getFromIndexMap("MAXBUCKETS") != null ? firstStep.getFromIndexMap("MAXBUCKETS") : null;
		final Integer PUFFERSIZE = firstStep.getFromIndexMap("PUFFERSIZE") != null ? firstStep.getFromIndexMap("PUFFERSIZE") : null;

		//Kann in einer Erweiterung hinzugefügt werden.
		//final Integer OVERSAMPLINGFACTOR = firstStep.getFromIndexMap("OVERSAMPLINGFACTOR") != null ? firstStep.getFromIndexMap("OVERSAMPLINGFACTOR") : null;
		//final Integer SINGLELEVELTHRESHOLD = firstStep.getFromIndexMap("SINGLELEVELTHRESHOLD") != null ? firstStep.getFromIndexMap("SINGLELEVELTHRESHOLD") : null;

		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream().map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new);

			//Kann in einer Erweiterung hinzugefügt werden.
			//samples = (sortingStep.getFromArrayListMap("samples") != null ? sortingStep.getFromArrayListMap("samples").stream().map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);

			splitters = (sortingStep.getFromArrayListMap("splitters") != null ? sortingStep.getFromArrayListMap("splitters").stream()
					.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);

			// Kann in einer Erweiterung hinzugefügt werden.
			//pufferIndices = sortingStep.getFromIndexArrayMap("pufferIndices");
			bktsize = sortingStep.getFromIndexArrayMap("bktsize");

			l = sortingStep.getFromIndexMap("l") != null ? sortingStep.getFromIndexMap("l") : 0;
			r = sortingStep.getFromIndexMap("r") != null ? sortingStep.getFromIndexMap("r") : size - 1;
			current = sortingStep.getFromIndexMap("current");
			bucket = sortingStep.getFromIndexMap("bucket");
			currentSwap = sortingStep.getFromIndexMap("currentSwap");
			readBucket = sortingStep.getFromIndexMap("readBucket");
			destBucket = sortingStep.getFromIndexMap("destBucket");
			element = sortingStep.getFromIndexMap("element");

			phase = IPS4o_PHASE.values()[sortingStep.getFromIndexMap("phase")];

			//bei Phasenwechsel: Schreibe die (neue) aktuelle Phase aus.
			if(currentPhase == null) {
				currentPhase = phase;
				texString += addVSpace(2);
				texString += addText(phase.toString().replace('_', ' '), TextLineOptions.TEXT_NEW_LINE,TextFontOptions.BOLD);
			}
			else if(phase != currentPhase) {
				currentPhase = phase;
				texString += addVSpace(2);
				texString += addText(phase.toString().replace('_', ' '), TextLineOptions.TEXT_NEW_LINE,TextFontOptions.BOLD);
			}


			puffers = new ArrayList<String[]>();
			for(int z = 0; z < MAXBUCKETS; z++) {
				String[] puffer = (sortingStep.getFromArrayListMap("puffer"+z) != null ? sortingStep.getFromArrayListMap("puffer"+z).stream()
						.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);
				puffers.add(puffer);
			}
			bucketPointers = new Integer[MAXBUCKETS][2];
			for(int z = 0; z < MAXBUCKETS; z++) {
				bucketPointers[z] =  (sortingStep.getFromIndexArrayMap("bucketPointer"+z) != null ? sortingStep.getFromIndexArrayMap("bucketPointer"+z) : null); 			
			}
			swapPuffers = new ArrayList<String[]>();
			for(int z = 0; z < 2; z++) {
				String[] swapPuffer = (sortingStep.getFromArrayListMap("swapPuffer"+z) != null ? sortingStep.getFromArrayListMap("swapPuffer"+z).stream()
						.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);
				swapPuffers.add(swapPuffer);
			}

			bucketStart = sortingStep.getFromIndexArrayMap("bucketStart") != null ? sortingStep.getFromIndexArrayMap("bucketStart") : null;

			List<Integer> pointerIndex = new ArrayList<Integer>();
			List<String> pointerText = new ArrayList<String>();
			List<String> pointerDownarrow = new ArrayList<String>();
			ArrayList<Integer> pivots = new ArrayList<Integer>();

			switch (phase) {
			case CONSTANTS:
				texString += addText(" BASECASESIZE: " + (BASECASESIZE != null ? BASECASESIZE : "-"),TextLineOptions.TEXT,TextFontOptions.TEXT);
				texString += addText(" SAMPLESIZE: " + (SAMPLESIZE != null ? SAMPLESIZE : "-"),TextLineOptions.TEXT,TextFontOptions.TEXT);
				texString += addText(" MAXBUCKETS: " + (MAXBUCKETS != null ? MAXBUCKETS : "-"), TextLineOptions.TEXT,TextFontOptions.TEXT);
				texString += addText(" PUFFERSIZE: " + (PUFFERSIZE != null ? PUFFERSIZE : "-"), TextLineOptions.TEXT_NEW_LINE,TextFontOptions.TEXT);

				//Kann in einer Erweiterung hinzugefügt werden.
				//texString += addText(" OVERSAMPLINGFACTOR: " + (OVERSAMPLINGFACTOR != null ? OVERSAMPLINGFACTOR : "-"), textOptions.TEXT,TextFontOptions.TEXT);
				//texString += addText(" SINGLELEVELTHRESHOLD: " + (SINGLELEVELTHRESHOLD != null ? SINGLELEVELTHRESHOLD : "-"), textOptions.TEXT,TextFontOptions.TEXT);

				pointerIndex = new ArrayList<Integer>();
				pointerIndex.add(l);
				pointerIndex.add(r);
				pointerText = new ArrayList<String>();
				pointerText.add("l");
				pointerText.add("r");
				pointerDownarrow = new ArrayList<String>();
				pointerDownarrow.add("$\\downarrow$");
				pointerDownarrow.add("$\\downarrow$");

				pivots = new ArrayList<Integer>();

				texString += beginTable(size, null,null,null);
				texString += getPointers(pointerIndex,pointerText,size,true); // l,r
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				texString += getArray(input, pivots,null);
				texString += endTable();
				break;

			case BUILD_TREE:
				if(legendCounter == 0) {
					legendCounter++;
					texString+=getLegend();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerIndex.add(l);
				pointerIndex.add(r);
				pointerText = new ArrayList<String>();
				pointerText.add("l");
				pointerText.add("r");
				pointerDownarrow = new ArrayList<String>();
				pointerDownarrow.add("$\\downarrow$");
				pointerDownarrow.add("$\\downarrow$");

				pivots = new ArrayList<Integer>();

				if(splitters != null) {
					texString += "\\begin{center}";
					texString += getTree(splitters);
					texString += "\\end{center}";
					for(int i = 0; i < splitters.length; i ++) {
						pivots.add(i);
					}
				}

				texString += beginTable(size, null,null,null);
				texString += getPointers(pointerIndex,pointerText,size,true); // l,r
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				texString += getArray(input, pivots,null);
				texString += endTable();
				texString += addVSpace(5);
				break;

			case LOCAL_CLASSIFICATION:
				if(legendCounter == 1) {
					legendCounter++;
					texString+=getLegend2();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerText = new ArrayList<String>();
				pointerDownarrow = new ArrayList<String>();

				pointerIndex.add(l);
				pointerIndex.add(r);

				pointerText.add("l");
				pointerText.add("r");

				pointerDownarrow.add("$\\downarrow$");
				pointerDownarrow.add("$\\downarrow$");

				if(element != null) {
					pointerIndex.add(element);
					pointerText.add("element");
					pointerDownarrow.add("$\\downarrow$");
				}

				if(current != null) {
					pointerIndex.add(current);
					pointerText.add("current");
					pointerDownarrow.add("$\\downarrow$");
				}

				texString += beginTable(size, null,null,null);
				texString += getPointers(pointerIndex,pointerText,size,true); // l,r
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				if(element != null) {
					texString += getArray(input, null,1,element,null);
				}
				else {
					texString += getArray(input, null,null);	
				}
				texString += endTable();
				texString += addVSpace(2);

				for(int z=0;z<MAXBUCKETS;z++) {
					String[] puffer = puffers.get(z);
					texString += beginTable(PUFFERSIZE, null,null,null);
					if(bucket != null  && bucket == z) {
						texString += getArray(puffer, null,PUFFERSIZE,0,null);
					}
					else {
						texString += getArray(puffer, null,null);
					}
					texString += endTable();
					texString += addVSpace(0.01);
				}
				break;

			case LOCAL_CLASSIFICATION_FULL_BUFFER:
				if(legendCounter == 1) {
					legendCounter++;
					texString+=getLegend2();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerText = new ArrayList<String>();
				pointerDownarrow = new ArrayList<String>();

				pointerIndex.add(l);
				pointerIndex.add(r);

				pointerText.add("l");
				pointerText.add("r");

				pointerDownarrow.add("$\\downarrow$");
				pointerDownarrow.add("$\\downarrow$");

				if(element != null) {
					pointerIndex.add(element);
					pointerText.add("element");
					pointerDownarrow.add("$\\downarrow$");
				}

				if(current != null) {
					pointerIndex.add(current);
					pointerText.add("current");
					pointerDownarrow.add("$\\downarrow$");
				}

				String[] colours = new String[2];

				colours[0] = "dunkelgrau";

				colours[1] = "hellblau";
				texString += beginTable(size, null,null,null);
				texString += getPointers(pointerIndex,pointerText,size,true); // l,r
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				if(current != null) {
					texString += getArray(input, null,3,current,colours);
				}
				else {
					texString += getArray(input, null,null);	
				}
				texString += endTable();
				texString += addVSpace(2);

				for(int z=0;z<MAXBUCKETS;z++) {
					String[] puffer = puffers.get(z);
					texString += beginTable(PUFFERSIZE, null,null,null);
					if(bucket != null  && bucket == z) {
						colours[1] = "rot";
						texString += getArray(puffer, null,PUFFERSIZE,0,colours);
					}
					else {
						texString += getArray(puffer, null,null);
					}
					texString += endTable();
					texString += addVSpace(0.1);
				}
				break;

			case BLOCK_PERMUTATION_POINTERS:
				if(legendCounter == 2) {
					legendCounter++;
					texString+=getLegend3();
				}
				//readBucket, maxOff, destBucket 
				pointerIndex = new ArrayList<Integer>();
				pointerDownarrow = new ArrayList<String>();
				pointerText = new ArrayList<String>();
				pointerIndex.add(l);
				pointerIndex.add(r);
				pointerText.add("l");
				pointerText.add("r");
				pointerDownarrow.add("$\\downarrow$");
				pointerDownarrow.add("$\\downarrow$");


				if(bucketPointers != null) {
					for(int z = 0; z < MAXBUCKETS; z++) {
						//TODO hier werden w_i und r_i eventuell nicht richtig dargestellt.
						pointerIndex.add(bucketPointers[z][0] < size ? bucketPointers[z][0] : size - 1);
						pointerText.add("w" + z);
						pointerDownarrow.add("$\\downarrow$");

						pointerIndex.add(bucketPointers[z][1] >= 0 ? bucketPointers[z][1] : 0);
						pointerText.add("r" + z);
						pointerDownarrow.add("$\\downarrow$");
					}
				}

				if(bucketStart != null) {
					for(int z = 0; z < MAXBUCKETS + 1; z++) {
						//TODO das letzte d_i wird hier z.B. falsch dargestellt.
						pointerIndex.add(bucketStart[z] < size ? bucketStart[z] : size - 1);
						pointerText.add("d" + z);
						pointerDownarrow.add("$\\downarrow$");
					}
				}

				if(readBucket != null) {
					pointerIndex.add(readBucket);
					pointerText.add("bRead");
					pointerDownarrow.add("$\\downarrow$");
				}
				if(destBucket != null) {
					pointerIndex.add(destBucket);
					pointerText.add("bDest");
					pointerDownarrow.add("$\\downarrow$");
				}

				ArrayList<Integer> seperators = new ArrayList<>();
				seperators.add(0);
				for(int i = 0; i < MAXBUCKETS; i++) {
					seperators.add(bucketStart[i]);	
				}
				seperators.add(size-1);
				texString += beginTable(size, seperators);
				texString += getPointers(pointerIndex,pointerText,size,true); // l,r
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				texString += getArray(input, null,null);	
				texString += endTable();
				texString += addVSpace(2);

				for(int z=0;z<2;z++) {
					String[] swapPuffer = swapPuffers.get(z);
					texString += beginTable(PUFFERSIZE, null,null,null);
					if(currentSwap != null && currentSwap == z) {
						pointerIndex = new ArrayList<Integer>();
						pointerIndex.add(0);
						pointerText = new ArrayList<String>();
						pointerText.add("currentSwap");
						pointerDownarrow = new ArrayList<String>();
						pointerDownarrow.add("$\\downarrow$");
						texString += getPointers(pointerIndex, pointerText, 1, true);
					}

					texString += getArray(swapPuffer, null,null);
					texString += endTable();
					texString += addVSpace(0.1);
				}
				break;

			case BLOCK_PERMUTATION_NEW_BUCKET:
				if(legendCounter == 2) {
					legendCounter++;
					texString+=getLegend3();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerDownarrow = new ArrayList<String>();
				pointerText = new ArrayList<String>();

				pointerIndex.add(bucketStart[destBucket]);
				pointerText.add("destBucket");
				pointerDownarrow.add("$\\downarrow$");

				pivots = new ArrayList<Integer>();
				pivots.add(l + current);

				seperators = new ArrayList<>();
				seperators.add(0);
				for(int i = 0; i < MAXBUCKETS; i++) {
					seperators.add(bucketStart[i]);
				}
				seperators.add(size-1);
				texString += beginTable(size, seperators);
				texString += getPointers(pointerIndex,pointerText,size,true); // destBucket
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				//bucket = newDestBucket
				texString += getArray(input, pivots ,bktsize[destBucket],bucketStart[destBucket],null);
				texString += endTable();
				texString += addVSpace(2);
				break;

			case BLOCK_PERMUTATION_READ_FROM_PRIMARY_BUCKET:
				if(legendCounter == 2) {
					legendCounter++;
					texString+=getLegend3();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerDownarrow = new ArrayList<String>();
				pointerText = new ArrayList<String>();

				seperators = new ArrayList<>();
				seperators.add(0);
				for(int i = 0; i < MAXBUCKETS; i++) {
					seperators.add(bucketStart[i]);
				}
				seperators.add(size-1);
				texString += beginTable(size, seperators);
				texString += getPointers(pointerIndex,pointerText,size,true); // destBucket
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				//bucket = newDestBucket
				texString += getArray(input, null,PUFFERSIZE,l + current,null);
				texString += endTable();
				texString += addVSpace(2);

				for(int z=0;z<2;z++) {
					String[] swapPuffer = swapPuffers.get(z);
					texString += beginTable(PUFFERSIZE, null,null,null);
					if(z == 0) {
						texString += getArray(swapPuffer, null,PUFFERSIZE,0,null);
					}
					else {
						texString += getArray(swapPuffer, null,null);
					}
					texString += endTable();
					texString += addVSpace(0.1);

				}
				break;

			case BLOCK_PERMUTATION_LOAD_IN:
				if(legendCounter == 2) {
					legendCounter++;
					texString+=getLegend3();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerDownarrow = new ArrayList<String>();
				pointerText = new ArrayList<String>();

				seperators = new ArrayList<>();
				seperators.add(0);
				for(int i = 0; i < MAXBUCKETS; i++) {
					seperators.add(bucketStart[i]);
				}
				seperators.add(size-1);
				texString += beginTable(size, seperators);
				texString += getPointers(pointerIndex,pointerText,size,true); // destBucket
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				//bucket = newDestBucket
				texString += getArray(input, null,PUFFERSIZE,l + current,null);
				texString += endTable();
				texString += addVSpace(2);

				for(int z=0;z<2;z++) {
					String[] swapPuffer = swapPuffers.get(z);
					texString += beginTable(PUFFERSIZE, null,null,null);
					if(currentSwap != null && currentSwap != z) {
						texString += getArray(swapPuffer, null,PUFFERSIZE,0,null);
					}
					else {
						texString += getArray(swapPuffer, null,null);
					}
					texString += endTable();
					texString += addVSpace(0.1);

				}
				break;

			case BLOCK_PERMUTATION_WRITE_IN:
				if(legendCounter == 2) {
					legendCounter++;
					texString+=getLegend3();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerDownarrow = new ArrayList<String>();
				pointerText = new ArrayList<String>();

				seperators = new ArrayList<>();
				seperators.add(0);
				for(int i = 0; i < MAXBUCKETS; i++) {
					seperators.add(bucketStart[i]);
				}
				seperators.add(size-1);
				texString += beginTable(size, seperators);
				texString += getPointers(pointerIndex,pointerText,size,true); // destBucket
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				//bucket = newDestBucket
				texString += getArray(input, null,3,l + current,null);
				texString += endTable();
				texString += addVSpace(2);

				for(int z=0;z<2;z++) {
					String[] swapPuffer = swapPuffers.get(z);
					texString += beginTable(PUFFERSIZE, null,null,null);
					if(currentSwap != null && currentSwap == z) {
						pointerIndex = new ArrayList<Integer>();
						pointerIndex.add(0);
						pointerText = new ArrayList<String>();
						pointerText.add("currentSwap");
						pointerDownarrow = new ArrayList<String>();
						pointerDownarrow.add("$\\downarrow$");
						texString += getPointers(pointerIndex, pointerText, 1, true);
						texString += getArray(swapPuffer, null,PUFFERSIZE,0,null);
					}
					else {
						texString += getArray(swapPuffer, null,null);
					}
					texString += endTable();
					texString += addVSpace(0.1);
				}

				break;

			case CLEANUP:
				if(legendCounter == 3) {
					legendCounter++;
					texString+=getLegend4();
				}
				pointerIndex = new ArrayList<Integer>();
				pointerIndex.add(l);
				pointerIndex.add(r);
				pointerText = new ArrayList<String>();
				pointerText.add("l");
				pointerText.add("r");
				pointerDownarrow = new ArrayList<String>();
				pointerDownarrow.add("$\\downarrow$");
				pointerDownarrow.add("$\\downarrow$");
				seperators = new ArrayList<>();
				seperators.add(0);
				for(int i = 0; i < MAXBUCKETS; i++) {
					seperators.add(bucketStart[i]);
				}
				seperators.add(size-1);
				texString += beginTable(size, seperators);
				texString += getPointers(pointerIndex,pointerText,size,true); // l,r
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
				texString += getArray(input, pivots,null);
				texString += endTable();
				texString += addVSpace(2);

				break;

			default:
				throw new IllegalArgumentException("Unbekannte Phase");
			}
		}
		return texString;
	}

	@Override
	public Collection<Node> getViewElements(SortingStep<I> sortingStep) {
		Collection<Node> c = new HashSet<Node>();
		Label inputLabel = new Label("input: " + (sortingStep.getFromArrayListMap("input") != null ? (sortingStep.getFromArrayListMap("input").stream()
				.map(e -> e.toString()).collect(Collectors.joining(" | "))) : "nicht gefunden"));
		Label lLabel = new Label("l: " + (sortingStep.getFromIndexMap("l") != null ? sortingStep.getFromIndexMap("l").toString() : "nicht gefunden"));
		Label rLabel = new Label("r: " + (sortingStep.getFromIndexMap("r") != null ? sortingStep.getFromIndexMap("r").toString() : "nicht gefunden"));

		c.add(inputLabel);
		c.add(lLabel);
		c.add(rLabel);
		return c;
	}

//	@Override
//	public String getTexIntro() {
//		String texIntro = "";
//		texIntro += addText("Todo: Individualisiertes Intro", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
//		return texIntro;
//	}

	@Override
	public String getLegend() {
		String texLegend = "";
		texLegend += addText("Legende erste Phase: Die erste Phase der Partitionierung, das Sampling. Diese ist\n" + 
				"analog zu dem Sampling der vorherigen Verfahren und soll die Grenzen der Buckets\n" + 
				"bestimmen. Das Vorgehen kann den vorherigen Abschnitten entnommen werden.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
	
	public String getLegend2() {
		String texLegend = "";
		texLegend += addText("Legende zweite Phase: Die zweite Phase der Partitionierung, die Lokale Klassifikation. Die Speicherung des\n" + 
				"aktuellen Elementes (angegeben durch den Zeiger element) im entsprechenden Puffer\n" + 
				"ist gelb hervorgehoben. Falls ein Puffer voll ist (rot hervorgehoben) muss dieser an die\n" + 
				"erste freie Position im Feld geschrieben werden (blau hervorgehoben). Diese Position\n" + 
				"wird durch den Zeiger current markiert. Der zu betrachtende Bereich der Sequenz ist\n" + 
				"wie gewohnt mit den Zeigern l und r begrenzt.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
	
	public String getLegend3() {
		String texLegend = "";
		texLegend += addText("Legende dritte Phase: Die dritte Phase der Partitionierung, die Block-Vertauschung. Die Zeiger\n" + 
				"ri;wi; di geben die einzelnen Grenzen der Invariante (siehe Abschnitt 6.4.2) an. Der zu\n" + 
				"betrachtende Bereich der Sequenz ist wie gewohnt mit den Zeigern l und r begrenzt.\n" + 
				"Der Zeiger bRead gibt den aktuellen Bucket an, aus welchem als nächstes in den\n" + 
				"Tauschpuffer geladen werden kann. Dieser Ladevorgang ist in gelb hervorgehoben.\n" + 
				"Der Zeiger bDest zeigt wiederum den Bucket an, in welchen der Block im Tauschpuffer\n" + 
				"geschrieben werden kann. Auch hier ist, diese Mal der Schreibvorgang, in gelb\n" + 
				"hervorgehoben. Dabei werden die Grenzen der einzelnen Buckets mit deutlicheren\n" + 
				"Linien markiert.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
	
	public String getLegend4() {
		String texLegend = "";
		texLegend += addText("Legende vierte Phase: Die vierte Phase der Partitionierung, die Bereinigung der Grenzen. "
				+ "Der zu betrachtende Bereich der Sequenz ist wie gewohnt mit den Zeigern l\n" + 
				"und r begrenzt. Dabei werden die Grenzen der einzelnen Buckets mit deutlicheren\n" + 
				"Linien markiert.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
}
