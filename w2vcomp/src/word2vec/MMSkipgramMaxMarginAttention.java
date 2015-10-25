package word2vec;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.word.Phrase;

import org.ejml.simple.SimpleMatrix;

import space.SemanticSpace;
import vocab.VocabEntry;

import common.HeatMapPanel;
import common.MathUtils;
import common.exception.ValueException;

import demo.TestConstants;

public class MMSkipgramMaxMarginAttention extends SingleThreadWord2Vec{
    
    
    public MMSkipgramMaxMarginAttention(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int negativeSamplesImages, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImages, subSample);
    }
    
    public MMSkipgramMaxMarginAttention(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int negativeSamplesImages , double subSample,  String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImages, subSample, menFile);
    }

    public void trainSentence(int[] sentenceSource, int[] sentenceTarget) {
        
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentenceSource.length;
        int iWordIndex = 0;
        

        boolean updateAtTheEnd=false;
        
        for (int wordPosition = 0; wordPosition < sentenceSource.length; wordPosition++) {

            int wordIndex = sentenceSource[wordPosition];

            // no way it will go here
            if (wordIndex == -1)
                continue;

            for (int i = 0; i < projectionLayerSize; i++) {
                a1[i] = 0;
                a1error[i] = 0;
            }

            if (vocab_lang1.getEntry(wordIndex).word.equals("bunny")){
                System.out.println("For Word "+vocab_lang1.getEntry(wordIndex).word);
            }
            
      
            
            //language 1
            //try to predict all words in the utterance
            for (int i = 0; i < sentenceSource.length; i++) {
                
                if (i==wordPosition){
                    continue;
                }
            
                int iPos = i;
                if (iPos < 0 || iPos >= sentenceLength)
                    continue;
                
                iWordIndex = sentenceSource[iPos];
                if (iWordIndex == -1)
                    continue;

                
                VocabEntry context = vocab_lang1.getEntry(iWordIndex);
                
                //if (vocab_lang1.getEntry(wordIndex).word.equals("bunny")){
                //    System.out.println("For Word "+vocab_lang1.getEntry(wordIndex).word+" predict "+context.word);
                //}
                
                // HIERARCHICAL SOFTMAX
                if (hierarchicalSoftmax) {
                    for (int bit = 0; bit < context.code.length(); bit++) {
                        double z2 = 0;
                        int iParentIndex = context.ancestors[bit];
                        // Propagate hidden -> output
                        for (int j = 0; j < projectionLayerSize; j++) {
                            z2 += weights0[wordIndex][j]
                                    * weights1[iParentIndex][j];
                        }

                        double a2 = sigmoidTable.getSigmoid(z2);
                        if (a2 == 0 || a2 == 1)
                            continue;
                        // "g" is the gradient multiplied by the learning rate
                        double gradient = (double) ((1 - (context.code
                                .charAt(bit) - 48) - a2) * alpha);
                        // Propagate errors output -> hidden
                        for (int j = 0; j < projectionLayerSize; j++) {
                            a1error[j] += gradient
                                    * weights1[iParentIndex][j];
                        }
                        // Learn weights hidden -> output
                        for (int j = 0; j < projectionLayerSize; j++) {
                            weights1[iParentIndex][j] += gradient * TestConstants.rate_multiplier_sft
                                    * weights0[wordIndex][j];
                        }
                    }
                }
                
             // NEGATIVE SAMPLING
                if (negativeSamples > 0) {
                    for (int l = 0; l < negativeSamples + 1; l++) {
                        int target;
                        int label;

                        if (l == 0) {
                            target = iWordIndex;
                            label = 1;
                        } else {
                            target = unigram.randomWordIndex();
                            if (target == 0) {
                                target = rand.nextInt(vocab_lang1.getVocabSize() - 1) + 1;
                            }
                            if (target == iWordIndex)
                                continue;
                            label = 0;
                        }
                        double z2 = 0;
                        double gradient;
                        for (int j = 0; j < projectionLayerSize; j++) {
                            z2 += weights0[wordIndex][j]
                                    * negativeWeights1[target][j];
                        }
                        double a2 = sigmoidTable.getSigmoid(z2);
                        
                        gradient = (double) ((label - a2) * alpha);
                        for (int j = 0; j < projectionLayerSize; j++) {
                            a1error[j] += gradient
                                    * negativeWeights1[target][j];
                        }
                        for (int j = 0; j < projectionLayerSize; j++) {
                            negativeWeights1[target][j] += gradient * TestConstants.rate_multiplier_sft
                                    * weights0[wordIndex][j];
                        }
                    }
                }
                // Learn weights input -> hidden
                if (!updateAtTheEnd){
                    for (int j = 0; j < projectionLayerSize; j++) {
                        weights0[wordIndex][j] += a1error[j];
                        a1error[j] = 0;

                    }
                }
                
            }
                    
            
           
        
 /*************    FOR TARGET LANGUAGE   ****************/
            
            SimpleMatrix a1error_temp = new SimpleMatrix(a1error.length, 1);
            if (negativeSamplesImages != -1) {
                
                double gradient=0;
                mmWordsPerRun++;
                
                SimpleMatrix mapped_word_row = (new SimpleMatrix(1,projectionLayerSize,false, weights0[wordIndex]));
                
              
              
                
                
                //try to come closer to all the words in the target language
                for (int wordPositionTarget = 0; wordPositionTarget < sentenceTarget.length; wordPositionTarget++) {
                    
                    //SimpleMatrix a1error_temp = new SimpleMatrix(a1error.length, 1);
                    
                    double normalization_over_words_in_sentence = 0.0;
                    
                    //set to 0 for every positive example
                    SimpleMatrix der = new SimpleMatrix(TestConstants.imageDimensions, 1);
                    
                    //specifics of current word
                    int wordIndexTarget = sentenceTarget[wordPositionTarget];
                    VocabEntry curWord = vocab_lang2.getEntry(wordIndexTarget);
                    int jPerceptIndex = images.getIndex(curWord.word);
                    
                    if (jPerceptIndex==-1){
                        //System.out.println(curWord.word+" not in visual space");
                        continue;
                    }
                    
                    //System.out.println("Visual Referent "+curWord.word);
                    SimpleMatrix image = new SimpleMatrix(negativeWeights1Images[jPerceptIndex].length,1,true, negativeWeights1Images[jPerceptIndex]);
                  
                    //first run to calculate normalization for score (w_t,I_i over all w_t in sentence
                    for (int ii = 0; ii < sentenceSource.length; ii++) {
                        
                        //get word
                        int ii_index = sentenceSource[ii];
                        //get word
                        SimpleMatrix ii_vector = (new SimpleMatrix(1,projectionLayerSize,false, weights0[ii_index]));
                        //calculate cosine between word and current image 
                        double cos = MathUtils.cosine(ii_vector, image);
                        normalization_over_words_in_sentence += Math.exp(cos);
                    }
                    
                    
                    SimpleMatrix err_cos_row = MathUtils.cosineDerivative(mapped_word_row, image);
                
                
                    double cos = MathUtils.cosine(mapped_word_row, image);
                    double err = 0.0;
                    
                    //NEGATIVE EXAMPLES
                    int k=0;
                    for (int l = 0; l < negativeSamplesImages ; l++) {
                        int neg_sample;
                        while (true){
                            neg_sample = images.randomWordIndex();       //random sampling and then based on neighboorhood
                            if (neg_sample != jPerceptIndex)
                                break;
                        }
                        SimpleMatrix image_neg = new SimpleMatrix(negativeWeights1Images[neg_sample].length,1,true, negativeWeights1Images[neg_sample]);
                        
                        double cos_neg = MathUtils.cosine(mapped_word_row, image_neg);
                        err -= Math.max(0, TestConstants.margin - cos+cos_neg);
                        if (cos-cos_neg >= TestConstants.margin) continue;
                        k+=1;
                    
                        //calculate error with respect to the cosine
                        der = der.minus(MathUtils.cosineDerivative(mapped_word_row, image_neg));
                    }
                    
                    
            
                     
                
                    gradient = (double) (alpha*  TestConstants.rate_multiplier_grad );
                    
                    der = der.plus(err_cos_row.scale(k));
                    
                    //score
                    double score = Math.exp(cos)/normalization_over_words_in_sentence;
                    //derivetive from f'g = (max_margin)'*score
                    a1error_temp  = a1error_temp.plus(der.scale((1-gradient)*score));
                    //derivative from g'f = (max_margin_error) * score'
                    //score' = exp(cos)*cos'*(SUM-epx(cos)) / SUM^2
                    SimpleMatrix der_score = (err_cos_row.scale(Math.exp(cos)).scale(normalization_over_words_in_sentence-Math.exp(cos))).divide(Math.pow(normalization_over_words_in_sentence,2));
                    a1error_temp  = a1error_temp.plus(der_score.scale(err*(1-gradient)));
                    
                    
                }
                // Learn weights input -> hidden
                for (int j = 0; j < projectionLayerSize; j++) {
                    weights0[wordIndex][j] += a1error_temp.get(j, 0);
                    a1error[j] = 0;
                }
                
            }
            /*
            //here see how this changes incrementally!
            List<String> OBJECTS = Arrays.asList("baby","bear","bird","book","bunny","cow","duck","hand","hat","kitty","lamb","mirror","pig","rattle","ring","sheep");
            List<String> WORDS = Arrays.asList("baby","bear","bigbird","bigbirds","bird","book","books","bunny","bunnies","bunnyrabbit","hiphop","cow","cows","moocow","moocows","duck","duckie","birdie","bird","hand","hat","kitty","kittycat","kittycats","meow","lamb","lambie","mirror","pig","piggie","piggies","oink","rattle","ring","rings","sheep");
           
            //SHOW only if cur word is in WORDS
            if (!WORDS.contains(vocab_lang1.getEntry(wordIndex).word)){
                continue;
            }
            SemanticSpace Im = images.space;
            SemanticSpace Words = new  SemanticSpace(vocab_lang1, weights0, false);
            
            

            double [][] sims = new double[WORDS.size()][OBJECTS.size()];
            int i=0;
            int j;
            //for every word
            for (String word: WORDS){
                j = 0;
                //for every object
                double s = 0;
                for (String object: OBJECTS){
                    sims[i][j] = Math.pow(Words.getSim(word, object, Im)+1,5);                    //sims[i][j] = Math.exp(Words.getSim(word, object, Im));
                    s += sims[i][j];
                    j++;
                }
                
                
                ////probability from similarities
                for (int jj=0;jj<OBJECTS.size();jj++){
                   sims[i][jj] /=s;
                }
                i++;
            }
            HeatMapPanel f = new HeatMapPanel(new SimpleMatrix(sims),WORD);
            
            */
            WORD++;
            }
            
     
        }

                
                
           
        
       

    

    public double[] getCors() throws ValueException{
        return images.pairwise_cor(new SemanticSpace(vocab_lang1, weights0,false));
    }

    @Override
    public void trainSinglePhrase(Phrase phrase, int[] sentence) {
        // TODO Auto-generated method stub
        
    }

  

    

   

}
