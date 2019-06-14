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
package org.apache.pinot.core.io.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.apache.pinot.core.segment.memory.PinotDataBuffer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link VarLengthBytesValueReaderWriter}
 */
public class VarLengthBytesValueReaderWriterTest {
  private final Random random = new Random();

  @Test
  public void testSingleByteArray() throws IOException {
    byte[] array = new byte[] {1, 2, 3, 4};
    byte[][] byteArrays = new byte[][]{array};
    long size = VarLengthBytesValueReaderWriter.getRequiredSize(byteArrays);
    Assert.assertEquals(20, size);

    final File tempFile = new File(FileUtils.getTempDirectory(),
        VarLengthBytesValueReaderWriterTest.class.getName() + random.nextInt());

    try (PinotDataBuffer buffer = PinotDataBuffer
        .mapFile(tempFile, false, 0, size, ByteOrder.BIG_ENDIAN, null)) {
      VarLengthBytesValueReaderWriter readerWriter = new VarLengthBytesValueReaderWriter(buffer);
      readerWriter.init(byteArrays);
      Assert.assertEquals(1, readerWriter.getNumElements());
    }

    try (PinotDataBuffer buffer = PinotDataBuffer
        .mapFile(tempFile, true, 0, size, ByteOrder.BIG_ENDIAN, null)) {
      VarLengthBytesValueReaderWriter readerWriter = new VarLengthBytesValueReaderWriter(buffer);
      Assert.assertEquals(1, readerWriter.getNumElements());
      byte[] newArray = readerWriter.getBytes(0, -1, null);
      Assert.assertTrue(Arrays.equals(array, newArray));
    }
    finally {
      FileUtils.forceDelete(tempFile);
    }
  }

  @Test
  public void testArbitraryLengthByteArray() throws IOException {
    Random random = new Random();
    int numByteArrays = random.nextInt(100);
    byte[][] byteArrays = new byte[numByteArrays][];
    for (int i = 0; i < numByteArrays; i++) {
      byteArrays[i] = new byte[i + 1];
      random.nextBytes(byteArrays[i]);
    }
    long size = VarLengthBytesValueReaderWriter.getRequiredSize(byteArrays);

    final File tempFile = new File(FileUtils.getTempDirectory(),
        VarLengthBytesValueReaderWriterTest.class.getName() + random.nextInt());

    try (PinotDataBuffer buffer = PinotDataBuffer
        .mapFile(tempFile, false, 0, size, ByteOrder.BIG_ENDIAN, null)) {
      VarLengthBytesValueReaderWriter readerWriter = new VarLengthBytesValueReaderWriter(buffer);
      readerWriter.init(byteArrays);
      Assert.assertEquals(byteArrays.length, readerWriter.getNumElements());
    }

    try (PinotDataBuffer buffer = PinotDataBuffer
        .mapFile(tempFile, false, 0, size, ByteOrder.BIG_ENDIAN, null)) {
      VarLengthBytesValueReaderWriter readerWriter = new VarLengthBytesValueReaderWriter(buffer);
      Assert.assertEquals(byteArrays.length, readerWriter.getNumElements());
      for (int i = 0; i < byteArrays.length; i++) {
        byte[] array = byteArrays[i];
        byte[] newArray = readerWriter.getBytes(i, -1, null);
        Assert.assertTrue(Arrays.equals(array, newArray));
      }
    }
    finally {
      FileUtils.forceDelete(tempFile);
    }
  }
}
