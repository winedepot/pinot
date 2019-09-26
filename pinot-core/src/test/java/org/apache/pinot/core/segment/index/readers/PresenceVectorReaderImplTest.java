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
package org.apache.pinot.core.segment.index.readers;

import org.apache.commons.io.FileUtils;
import org.apache.pinot.core.segment.creator.impl.presence.PresenceVectorCreator;
import org.apache.pinot.core.segment.memory.PinotDataBuffer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class PresenceVectorReaderImplTest {
    private static final File TEMP_DIR = new File(FileUtils.getTempDirectory(), "PresenceVectorCreatorTest");
    private static final String COLUMN_NAME = "test";

    @BeforeClass
    public void setup() {
        if (TEMP_DIR.exists()) {
            FileUtils.deleteQuietly(TEMP_DIR);
        }
        TEMP_DIR.mkdir();
        try (PresenceVectorCreator creator = new PresenceVectorCreator(TEMP_DIR, COLUMN_NAME)) {
            for (int i = 0; i < 100; i++) {
                creator.setNull(i);
            }
        } catch (IOException e) {
            Assert.fail("Unable to create PresenceVectorCreator", e);
        }
    }

    @Test
    public void testPresenceVectorReader() {
        Assert.assertEquals(TEMP_DIR.list().length, 1);
        File presenceFile = new File(TEMP_DIR, TEMP_DIR.list()[0]);
        try {
            PinotDataBuffer buffer = PinotDataBuffer.loadBigEndianFile(presenceFile);
            PresenceVectorReader reader = new PresenceVectorReaderImpl(buffer);
            for (int i = 0; i < 100; i++) {
                Assert.assertFalse(reader.isPresent(i));
            }
        } catch (IOException e) {
            Assert.fail("Unable to create PresenceVectorReader from given file", e);
        }
    }

    @AfterClass
    public void tearDown()
            throws Exception {
        FileUtils.deleteDirectory(TEMP_DIR);
    }
}
