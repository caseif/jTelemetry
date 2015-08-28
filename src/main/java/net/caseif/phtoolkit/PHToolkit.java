/*
 * New BSD License (BSD-new)
 *
 * Copyright (c) 2015 Maxim Roncacé
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.caseif.phtoolkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight Java toolkit for submitting arbitrary information to a web server
 * (i.e. "phoning home").
 *
 * @author Max Roncace
 * @version 1.0.0
 */
public class PHToolkit {

    private final String recipient;

    /**
     * Constructs a new {@link PHToolkit} object with the given recipient
     * address. This address must have an HTTP or HTTPS protocol.
     *
     * @param recipient The recipient address of this {@link PHToolkit}
     * @throws IllegalArgumentException If the address's protocol is not valid
     * @since 1.0
     */
    public PHToolkit(String recipient) throws IllegalArgumentException {
        this.recipient = recipient;
    }

    /**
     * Returns the recipient address of this {@link PHToolkit}.
     *
     * @return The recipient address of this {@link PHToolkit}
     * @since 1.0
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Creates a new {@link Payload} parented by this {@link PHToolkit}.
     *
     * @return The new {@link Payload}
     * @since 1.0
     */
    public Payload createPayload() {
        return new Payload(this);
    }

    /**
     * Represents a payload containing data entries to be submitted to a server
     * in a single request.
     *
     * @since 1.0
     */
    public class Payload {

        private PHToolkit parent;

        private Map<String, DataEntry> dataMap = new HashMap<>();

        private Payload(PHToolkit parent) {
            this.parent = parent;
        }

        /**
         * Returns the parent {@link PHToolkit} of this {@link Payload}.
         *
         * @return The parent {@link PHToolkit} of this {@link Payload}
         * @since 1.0
         */
        public PHToolkit getParent() {
            return parent;
        }

        /**
         * Attempts to submit this payload to the address defined by the parent
         * {@link PHToolkit}.
         *
         * @return The HTTP response code returned by the server
         * @throws IOException If an exception occurs while sending a request to
         *     the remote server
         * @since 1.0
         */
        public int submit() throws IOException {
            URL url = new URL(getParent().getRecipient());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            byte[] serial = serializeData();
            conn.setRequestProperty("Content-Length", Integer.toString(serial.length));
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(serial);
            }
            return conn.getResponseCode();
        }

        private byte[] serializeData() {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // magic number for verification purposes
                byte[] magic = new byte[]{(byte) 0xB0, (byte) 0x00, (byte) 0xB1, (byte) 0xE5};
                out.write(magic);
                for (DataEntry entry : dataMap.values()) {
                    byte[] serial = entry.serialize();
                    out.write(serial);
                }
                return out.toByteArray();
            } catch (IOException ex) {
                throw new AssertionError(ex);
            }
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a boolean
         * (1-bit integer).
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, boolean data) throws IllegalArgumentException {
            addData(DataType.BOOLEAN, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a boolean
         * (1-bit integer).
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, byte data) throws IllegalArgumentException {
            addData(DataType.BOOLEAN, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a short
         * (16-bit) signed integer.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, short data) throws IllegalArgumentException {
            addData(DataType.SHORT, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a 32-bit
         * signed integer.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, int data) throws IllegalArgumentException {
            addData(DataType.INT, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a long
         * (64-bit) signed integer.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, long data) throws IllegalArgumentException {
            addData(DataType.LONG, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of an IEEE
         * floating-point number.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, float data) throws IllegalArgumentException {
            addData(DataType.FLOAT, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of an IEEE
         * double-precision floating point number.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, double data) throws IllegalArgumentException {
            addData(DataType.DOUBLE, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a string.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key is {@code null}
         * @since 1.0
         */
        public void addData(String key, String data) throws IllegalArgumentException {
            addData(DataType.STRING, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a 32-bit
         * integer array.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key or data is
         *     {@code null}
         * @since 1.0
         */
        public void addData(String key, int[] data) throws IllegalArgumentException {
            addData(DataType.ARRAY, key, data);
        }

        /**
         * Adds a piece of data to this {@link Payload} in the form of a string
         * array.
         *
         * @param key The key to assign to the data
         * @param data The content of the data
         * @throws IllegalArgumentException If this payload already contains
         *     a data entry with the given key, or if the key, data, or any
         *     element of the data array is {@code null}
         * @since 1.0
         */
        public void addData(String key, String[] data) throws IllegalArgumentException {
            DataEntry[] array = new DataEntry[data.length];
            for (int i = 0; i < data.length; i++) {
                array[i] = new DataEntry(DataType.STRING, null, data[i]);
            }
            addData(DataType.ARRAY, key, array);
        }

        private void addData(DataType type, String key, Object data) throws IllegalArgumentException {
            assert type != null;
            if (key == null) {
                throw new IllegalArgumentException("Key must not be null");
            }
            if (data == null) {
                throw new IllegalArgumentException("Content must not be null");
            }
            if (dataMap.containsKey(key)) {
                throw new IllegalArgumentException("Cannot redefine key \"" + key + "\" within the payload");
            }
            dataMap.put(key, new DataEntry(type, key, data));
        }

        private class DataEntry {

            private DataType type;
            private String key;
            private Object content;

            private DataEntry(DataType type, String key, Object content) throws IllegalArgumentException {
                this.type = type;
                this.key = key;
                this.content = content;
            }

            private byte[] serialize() throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                out.write((byte) type.ordinal()); // enter the type of content

                if (key != null) { // only true if this is an element of an array
                    // enter the key
                    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                    out.write(ByteUtils.toBytes(keyBytes.length)); // enter key length
                    out.write(keyBytes); // enter key content
                }

                if (type == DataType.ARRAY) { // special handling for arrays
                    DataEntry[] entries = (DataEntry[]) content;
                    // serialize each entry and put it in a temp stream
                    ByteArrayOutputStream temp = new ByteArrayOutputStream();
                    for (DataEntry entry : entries) {
                        byte[] bytes = entry.serialize();
                        temp.write(bytes);
                    }
                    byte[] bytes = temp.toByteArray();
                    out.write(ByteUtils.toBytes(bytes.length)); // write the length of the entire array
                    out.write(ByteUtils.toBytes(entries.length)); // write the number of entries
                    out.write(bytes); // write the entries themselves
                } else if (type == DataType.STRING) { // special handling for strings
                    // convert the string to a byte array
                    byte[] strBytes = ((String) content).getBytes(StandardCharsets.UTF_8);
                    out.write(ByteUtils.toBytes(strBytes.length)); // enter the string length
                    out.write(strBytes); // enter the string data
                } else { // default handling
                    assert type.getLength() != -1; // program shouldn't progress to here if content length isn't fixed
                    // check the type and insert the content
                    switch (type) {
                        case BOOLEAN: {
                            out.write((boolean) content ? 1 : 0);
                            break;
                        }
                        case BYTE: {
                            out.write((byte) content);
                            break;
                        }
                        case SHORT: {
                            out.write(ByteUtils.toBytes((short) content));
                            break;
                        }
                        case INT: {
                            out.write(ByteUtils.toBytes((int) content));
                            break;
                        }
                        case LONG: {
                            out.write(ByteUtils.toBytes((long) content));
                            break;
                        }
                        case FLOAT: {
                            out.write(ByteUtils.toBytes((float) content));
                            break;
                        }
                        case DOUBLE: {
                            out.write(ByteUtils.toBytes((double) content));
                            break;
                        }
                        default: {
                            throw new AssertionError();
                        }
                    }
                }
                return out.toByteArray();
            }

        }

    }

    /**
     * Represents a type of data.
     *
     * @since 1.0
     */
    private enum DataType {
        /**
         * A boolean (1-bit integer) value.
         *
         * @since 1.0
         */
        BOOLEAN(1),
        /**
         * An 8-bit byte value
         *
         * @since 1.0
         */
        BYTE(1),
        /**
         * A short (16-bit) integer value.
         *
         * @since 1.0
         */
        SHORT(2),
        /**
         * A short (32-bit) integer value.
         *
         * @since 1.0
         */
        INT(4),
        /**
         * A long (64-bit) integer value.
         *
         * @since 1.0
         */
        LONG(8),
        /**
         * A IEEE single-precision (32-bit) floating point number
         *
         * @since 1.0
         */
        FLOAT(4),
        /**
         * A IEEE double-precision (64-bit) floating point number
         *
         * @since 1.0
         */
        DOUBLE(8),
        /**
         * A UTF-8 string. String data entries are to be prefixed with a 32-bit integer representing their respective
         * length.
         *
         * @since 1.0
         */
        STRING(-1),
        /**
         * An array of objects. Array-type data entries are to be prefixed with a 32-bit signed integer representing
         * their respective length (i.e. the number of entries contained).
         *
         * @since 1.0
         */
        ARRAY(-1);

        private final int length;

        DataType(int length) {
            this.length = length;
        }

        private int getLength() {
            return length;
        }

    }

    private static class ByteUtils {

        private static final ByteBuffer SHORT_BUFFER = ByteBuffer.allocate(2);
        private static final ByteBuffer MID_BUFFER = ByteBuffer.allocate(4);
        private static final ByteBuffer LONG_BUFFER = ByteBuffer.allocate(8);

        public static byte[] toBytes(short x) {
            SHORT_BUFFER.putInt(0, x);
            return SHORT_BUFFER.array();
        }

        public static byte[] toBytes(int x) {
            MID_BUFFER.putInt(0, x);
            return MID_BUFFER.array();
        }

        public static byte[] toBytes(long x) {
            LONG_BUFFER.putLong(0, x);
            return LONG_BUFFER.array();
        }

        public static byte[] toBytes(float x) {
            MID_BUFFER.putFloat(0, x);
            return MID_BUFFER.array();
        }

        public static byte[] toBytes(double x) {
            LONG_BUFFER.putDouble(0, x);
            return LONG_BUFFER.array();
        }
    }

}
