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
package org.apache.pinot.core.data.recordtransformer;

import java.util.Collection;
import org.apache.pinot.common.data.FieldSpec;
import org.apache.pinot.common.data.Schema;
import org.apache.pinot.core.data.GenericRow;
import org.roaringbitmap.RoaringBitmap;

import static org.apache.pinot.common.utils.CommonConstants.Segment.NULL_FIELDS;


public class NullValueTransformer implements RecordTransformer {
  private final Collection<FieldSpec> _fieldSpecs;
  private final Schema _schema;

  public NullValueTransformer(Schema schema) {
    _schema = schema;
    _fieldSpecs = schema.getAllFieldSpecs();
  }

  @Override
  public GenericRow transform(GenericRow record) {
    RoaringBitmap nullColumnBitMap = null;

    for (FieldSpec fieldSpec : _fieldSpecs) {
      String fieldName = fieldSpec.getName();
      // Do not allow default value for time column
      if (record.getValue(fieldName) == null && fieldSpec.getFieldType() != FieldSpec.FieldType.TIME) {
        // Only handle null columns for non-virtual columns
        if (!fieldSpec.isVirtualColumn()) {
          if (nullColumnBitMap == null) {
            nullColumnBitMap = new RoaringBitmap();
          }
          nullColumnBitMap.add(_schema.getColumnId(fieldName));
        }

        if (fieldSpec.isSingleValueField()) {
          record.putField(fieldName, fieldSpec.getDefaultNullValue());
        } else {
          record.putField(fieldName, new Object[]{fieldSpec.getDefaultNullValue()});
        }
      }
    }

    record.putField(NULL_FIELDS, nullColumnBitMap);
    return record;
  }
}
