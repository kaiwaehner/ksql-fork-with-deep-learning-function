/**
 * Copyright 2017 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.confluent.ksql.function.udf.ml;

import java.util.Arrays;

import hex.genmodel.GenModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.AutoEncoderModelPrediction;
import io.confluent.ksql.function.KsqlFunctionException;
import io.confluent.ksql.function.udf.Kudf;


public class AnomalyKudf implements Kudf {

  // Model built with H2O R API:
  // anomaly_model <- h2o.deeplearning(x = names(train_ecg),training_frame =
  // train_ecg,activation = "Tanh",autoencoder = TRUE,hidden =
  // c(50,20,50),sparse = TRUE,l1 = 1e-4,epochs = 100)

  // Name of the generated H2O model
  private static String modelClassName = "io.confluent.ksql.function.udf.ml"
                                         + ".DeepLearning_model_R_1509973865970_1";


  @Override
  public Object evaluate(Object... args) {
    if (args.length != 1) {
      throw new KsqlFunctionException("Anomaly udf should have one input argument.");
    }
    try {
      return applyAnalyticModel(args[0]);

    } catch (Exception e) {
      throw new KsqlFunctionException("Model Inference failed. Please check the logs.");
    }
  }

  private Object applyAnalyticModel(Object object) throws Exception {

    GenModel rawModel;
    rawModel = (hex.genmodel.GenModel) Class.forName(modelClassName).newInstance();
    EasyPredictModelWrapper model = new EasyPredictModelWrapper(rawModel);
    
    // Prepare input sensor data to be in correct data format for the autoencoder model (double[]):
	String inputWithHash = (String) object;
	String[] inputStringArray = inputWithHash.split("#");
	double[] doubleValues = Arrays.stream(inputStringArray)
            .mapToDouble(Double::parseDouble)
            .toArray();
    
    RowData row = new RowData();
    int j = 0;
    for (String colName : rawModel.getNames()) {
      row.put(colName, doubleValues[j]);
      j++;
    }

    AutoEncoderModelPrediction p = model.predictAutoEncoder(row);
    // System.out.println("original: " + java.util.Arrays.toString(p.original));
    // System.out.println("reconstructedrowData: " + p.reconstructedRowData);
    // System.out.println("reconstructed: " + java.util.Arrays.toString(p.reconstructed));

    double sum = 0;
    for (int i = 0; i < p.original.length; i++) {
      sum += (p.original[i] - p.reconstructed[i]) * (p.original[i] - p.reconstructed[i]);
    }
    // Calculate Mean Square Error => High reconstruction error means anomaly
    double mse = sum / p.original.length;
    // System.out.println("MSE: " + mse);

    String mseString = "" + mse;

    return (mseString);
  }
}
