package org.apache.pinot.orc.data.readers;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.apache.pinot.common.data.DimensionFieldSpec;
import org.apache.pinot.common.data.FieldSpec;
import org.apache.pinot.common.data.Schema;
import org.apache.pinot.core.data.GenericRow;
import org.apache.pinot.core.indexsegment.generator.SegmentGeneratorConfig;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ORCRecordReaderTest {
  private static final File TEMP_DIR = FileUtils.getTempDirectory();
  private static final File ORC_FILE = new File(TEMP_DIR.getAbsolutePath(), "my-file.orc");

  @BeforeClass
  public void setUp()
      throws Exception {
    FileUtils.deleteQuietly(TEMP_DIR);
    TypeDescription schema =
        TypeDescription.fromString("struct<x:int,y:string>");

    Writer writer = OrcFile.createWriter(new Path(ORC_FILE.getAbsolutePath()),
        OrcFile.writerOptions(new Configuration())
            .setSchema(schema));

    VectorizedRowBatch batch = schema.createRowBatch();
    LongColumnVector x = (LongColumnVector) batch.cols[0];
    BytesColumnVector y = (BytesColumnVector) batch.cols[1];
    for(int r=0; r < 5; ++r) {
      int row = batch.size++;
      x.vector[row] = r;
      byte[] buffer = ("Last-" + (r * 3)).getBytes(StandardCharsets.UTF_8);
      y.setRef(row, buffer, 0, buffer.length);
      // If the batch is full, write it out and start over.
      if (batch.size == batch.getMaxSize()) {
        writer.addRowBatch(batch);
        batch.reset();
      }
    }
    if (batch.size != 0) {
      writer.addRowBatch(batch);
    }
    writer.close();
  }

  @Test
  public void testReadData()
      throws IOException {

    ORCRecordReader orcRecordReader = new ORCRecordReader();

    SegmentGeneratorConfig segmentGeneratorConfig = new SegmentGeneratorConfig();
    segmentGeneratorConfig.setInputFilePath(ORC_FILE.getAbsolutePath());
    Schema schema = new Schema();
    FieldSpec xFieldSpec = new DimensionFieldSpec("x", FieldSpec.DataType.LONG, true);
    schema.addField(xFieldSpec);
    FieldSpec yFieldSpec = new DimensionFieldSpec("y", FieldSpec.DataType.BYTES, true);
    schema.addField(yFieldSpec);
    segmentGeneratorConfig.setSchema(schema);
    orcRecordReader.init(segmentGeneratorConfig);

    List<GenericRow> genericRows = new ArrayList<>();
    while (orcRecordReader.hasNext()) {
      genericRows.add(orcRecordReader.next());
    }
    orcRecordReader.close();
    Assert.assertEquals(genericRows.size(), 5, "Generic row size must be 5");

    for (int i = 0; i < genericRows.size(); i++) {
      Assert.assertEquals(genericRows.get(i).getValue("x"), i);
      Assert.assertEquals(genericRows.get(i).getValue("y"), "Last-" + (i * 3));
    }
  }

  @AfterClass
  public void tearDown() {
    FileUtils.deleteQuietly(TEMP_DIR);
  }

}
