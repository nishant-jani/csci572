/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.sax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for the {@link org.apache.tika.sax.BodyContentHandler} class.
 */
public class BasicContentHandlerFactoryTest {
    private static final String ENCODING = "UTF-8";
    //default max char len (at least in WriteOutContentHandler is 100k)
    private static final int OVER_DEFAULT = 120000;

    @Test
    public void testIgnore() throws Exception {
        Parser p = new MockParser(OVER_DEFAULT);
        ContentHandler handler =
                new BasicContentHandlerFactory(BasicContentHandlerFactory.HANDLER_TYPE.IGNORE, -1).getNewContentHandler();
        assertTrue(handler instanceof DefaultHandler);
        p.parse(null, handler, null, null);
        assertTrue(handler.toString().contains(""));

        //tests that no write limit exception is thrown
        p = new MockParser(100);
        handler =
                new BasicContentHandlerFactory(BasicContentHandlerFactory.HANDLER_TYPE.IGNORE, 5).getNewContentHandler();
        assertTrue(handler instanceof DefaultHandler);
        p.parse(null, handler, null, null);
        assertTrue(handler.toString().contains(""));
    }

    @Test
    public void testText() throws Exception {
        Parser p = new MockParser(OVER_DEFAULT);
        BasicContentHandlerFactory.HANDLER_TYPE type = 
                BasicContentHandlerFactory.HANDLER_TYPE.TEXT;
        ContentHandler handler =
                new BasicContentHandlerFactory(type, -1).getNewContentHandler();

        assertTrue(handler instanceof ToTextContentHandler);
        p.parse(null, handler, null, null);
        assertTrue(handler.toString().contains("This is the title"));
        assertTrue(handler.toString().contains("aaaaaaaaaa"));
        assertFalse(handler.toString().toLowerCase().contains("<body"));
        assertFalse(handler.toString().toLowerCase().contains("<html"));
        assertTrue(handler.toString().length() > 110000);
        //now test write limit
        p = new MockParser(10);
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler();
        assertTrue(handler instanceof WriteOutContentHandler);
        assertWriteLimitReached(p, (WriteOutContentHandler) handler);
        assertTrue(handler.toString().contains("This "));
        assertFalse(handler.toString().toLowerCase().contains("aaaa"));

        //now test outputstream call
        p = new MockParser(OVER_DEFAULT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, -1).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof ToTextContentHandler);
        p.parse(null, handler, null, null);
        assertContains("This is the title", os.toByteArray());
        assertContains("aaaaaaaaaa", os.toByteArray());
        assertTrue(os.toByteArray().length > 110000);
        assertNotContains("<body", os.toByteArray());
        assertNotContains("<html", os.toByteArray());

        p = new MockParser(10);
        os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof WriteOutContentHandler);
        assertWriteLimitReached(p, (WriteOutContentHandler) handler);
        //When writing to an OutputStream and a write limit is reached,
        //currently, nothing is written.
        assertEquals(0, os.toByteArray().length);
    }


    @Test
    public void testHTML() throws Exception {
        Parser p = new MockParser(OVER_DEFAULT);
        BasicContentHandlerFactory.HANDLER_TYPE type =
                BasicContentHandlerFactory.HANDLER_TYPE.HTML;
        ContentHandler handler =
                new BasicContentHandlerFactory(type, -1).getNewContentHandler();

        assertTrue(handler instanceof ToHTMLContentHandler);
        p.parse(null, handler, null, null);
        assertTrue(handler.toString().contains("<head><title>This is the title"));
        assertTrue(handler.toString().contains("aaaaaaaaaa"));
        assertTrue(handler.toString().length() > 110000);

        //now test write limit
        p = new MockParser(10);
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler();
        assertTrue(handler instanceof WriteOutContentHandler);
        assertWriteLimitReached(p, (WriteOutContentHandler) handler);
        assertTrue(handler.toString().contains("This "));
        assertFalse(handler.toString().toLowerCase().contains("aaaa"));

        //now test outputstream call
        p = new MockParser(OVER_DEFAULT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, -1).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof ToHTMLContentHandler);
        p.parse(null, handler, null, null);
        assertContains("This is the title", os.toByteArray());
        assertContains("aaaaaaaaaa", os.toByteArray());
        assertContains("<body", os.toByteArray());
        assertContains("<html", os.toByteArray());
        assertTrue(os.toByteArray().length > 110000);


        p = new MockParser(10);
        os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof WriteOutContentHandler);
        assertWriteLimitReached(p, (WriteOutContentHandler) handler);
        assertEquals(0, os.toByteArray().length);
    }

    @Test
    public void testXML() throws Exception {
        Parser p = new MockParser(OVER_DEFAULT);
        BasicContentHandlerFactory.HANDLER_TYPE type =
                BasicContentHandlerFactory.HANDLER_TYPE.HTML;
        ContentHandler handler =
                new BasicContentHandlerFactory(type, -1).getNewContentHandler();

        assertTrue(handler instanceof ToXMLContentHandler);
        p.parse(null, handler, new Metadata(), null);
        assertTrue(handler.toString().contains("<head><title>This is the title"));
        assertTrue(handler.toString().contains("aaaaaaaaaa"));
        assertTrue(handler.toString().length() > 110000);

        //now test write limit
        p = new MockParser(10);
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler();
        assertTrue(handler instanceof WriteOutContentHandler);
        assertWriteLimitReached(p, (WriteOutContentHandler) handler);
        assertTrue(handler.toString().contains("This "));
        assertFalse(handler.toString().toLowerCase().contains("aaaa"));

        //now test outputstream call
        p = new MockParser(OVER_DEFAULT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, -1).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof ToXMLContentHandler);
        p.parse(null, handler, null, null);
        assertContains("This is the title", os.toByteArray());
        assertContains("aaaaaaaaaa", os.toByteArray());
        assertContains("<body", os.toByteArray());
        assertContains("<html", os.toByteArray());
        assertTrue(os.toByteArray().length > 110000);


        p = new MockParser(10);
        os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof WriteOutContentHandler);
        assertWriteLimitReached(p, (WriteOutContentHandler) handler);
        assertEquals(0, os.toByteArray().length);
    }


    @Test
    public void testBody() throws Exception {
        Parser p = new MockParser(OVER_DEFAULT);
        BasicContentHandlerFactory.HANDLER_TYPE type =
                BasicContentHandlerFactory.HANDLER_TYPE.BODY;
        ContentHandler handler =
                new BasicContentHandlerFactory(type, -1).getNewContentHandler();

        assertTrue(handler instanceof BodyContentHandler);

        p.parse(null, handler, null, null);
        assertFalse(handler.toString().contains("title"));
        assertTrue(handler.toString().contains("aaaaaaaaaa"));
        assertTrue(handler.toString().length() > 110000);

        //now test write limit
        p = new MockParser(10);
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler();
        assertTrue(handler instanceof BodyContentHandler);
        assertWriteLimitReached(p, (BodyContentHandler)handler);
        assertFalse(handler.toString().contains("This "));
        assertTrue(handler.toString().toLowerCase().contains("aaaa"));

        //now test outputstream call
        p = new MockParser(OVER_DEFAULT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, -1).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof BodyContentHandler);
        p.parse(null, handler, null, null);
        assertNotContains("title", os.toByteArray());
        assertContains("aaaaaaaaaa", os.toByteArray());
        assertNotContains("<body", os.toByteArray());
        assertNotContains("<html", os.toByteArray());
        assertTrue(os.toByteArray().length > 110000);

        p = new MockParser(10);
        os = new ByteArrayOutputStream();
        handler = new BasicContentHandlerFactory(type, 5).getNewContentHandler(os, ENCODING);
        assertTrue(handler instanceof WriteOutContentHandler);
        assertWriteLimitReached(p, (WriteOutContentHandler) handler);
        assertEquals(0, os.toByteArray().length);
    }

    private void assertWriteLimitReached(Parser p, WriteOutContentHandler handler) throws Exception {
        boolean wlr = false;
        try {
            p.parse(null, handler, null, null);
        } catch (SAXException e) {
            if (! handler.isWriteLimitReached(e)) {
                throw e;
            }
            wlr = true;
        }
        assertTrue("WriteLimitReached", wlr);
    }
    //TODO: is there a better way than to repeat this with diff signature?
    private void assertWriteLimitReached(Parser p, BodyContentHandler handler) throws Exception {
        boolean wlr = false;
        try {
            p.parse(null, handler, null, null);
        } catch (SAXException e) {
            if (! e.getClass().toString().contains("org.apache.tika.sax.WriteOutContentHandler$WriteLimitReachedException")){
                throw e;
            }

            wlr = true;
        }
        assertTrue("WriteLimitReached", wlr);
    }

    private void assertNotContains(String needle, byte[] hayStack)
            throws UnsupportedEncodingException {
        String s  = new String(hayStack, ENCODING);
        assertFalse(s.toLowerCase().contains(needle));
    }

    private void assertContains(String needle, byte[] hayStack)
            throws UnsupportedEncodingException {
        String s  = new String(hayStack, ENCODING);
        assertTrue(s.contains(needle));
    }

    //Simple mockparser that writes a title
    //and charsToWrite number of 'a'
    private class MockParser implements Parser {
        private final String XHTML = "http://www.w3.org/1999/xhtml";
        private final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
        private final char[] TITLE = "This is the title".toCharArray();

        private final int charsToWrite;
        public MockParser(int charsToWrite) {
            this.charsToWrite = charsToWrite;
        }

        @Override
        public Set<MediaType> getSupportedTypes(ParseContext context) {
            return null;
        }

        @Override
        public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
            handler.startDocument();
            handler.startPrefixMapping("", XHTML);
            handler.startElement(XHTML, "html", "html", EMPTY_ATTRIBUTES);
            handler.startElement(XHTML, "head", "head", EMPTY_ATTRIBUTES);
            handler.startElement(XHTML, "title", "head", EMPTY_ATTRIBUTES);
            handler.characters(TITLE, 0, TITLE.length);
            handler.endElement(XHTML, "title", "head");

            handler.endElement(XHTML, "head", "head");
            handler.startElement(XHTML, "body", "body", EMPTY_ATTRIBUTES);
            char[] body = new char[charsToWrite];
            for (int i = 0; i < charsToWrite; i++) {
                body[i] = 'a';
            }
            handler.characters(body, 0, body.length);
            handler.endElement(XHTML, "body", "body");
            handler.endElement(XHTML, "html", "html");
            handler.endDocument();
        }
    }
}
