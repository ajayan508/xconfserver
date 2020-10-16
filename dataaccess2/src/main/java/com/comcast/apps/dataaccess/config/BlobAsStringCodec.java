/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * 
 */
package com.comcast.apps.dataaccess.config;

import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.extras.codecs.MappingCodec;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BlobAsStringCodec extends MappingCodec<String, ByteBuffer> {

    public BlobAsStringCodec() { super(TypeCodec.blob(), String.class); }

    @Override
    protected ByteBuffer serialize(String value) {
        byte[] bytes = value.getBytes(UTF_8);
        return ByteBuffer.wrap(bytes);
    }

    @Override
    protected String deserialize(ByteBuffer value) {
        return UTF_8.decode(value).toString();
    }
}
