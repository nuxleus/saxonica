package net.sf.saxon.javax.xml.xquery;



/**
 * XQJ interfaces reconstructed from version 0.5 documentation
 */
public interface XQSequence extends XQItemAccessor {

    boolean absolute(int itempos) throws XQException;

    void afterLast() throws XQException;

    void beforeFirst() throws XQException;

    void close() throws XQException;

    int count() throws XQException;

    boolean first() throws XQException;

    XQItem getItem() throws XQException;

    int getPosition() throws XQException;

    javax.xml.stream.XMLStreamReader getSequenceAsStream() throws XQException;

    java.lang.String getSequenceAsString(java.util.Properties props) throws XQException;

    boolean isAfterLast() throws XQException;

    boolean isBeforeFirst() throws XQException;

    boolean isClosed();

    boolean isFirst() throws XQException;

    boolean isLast() throws XQException;

    boolean isOnItem() throws XQException;

    boolean isScrollable() throws XQException;

    boolean last() throws XQException;

    boolean next() throws XQException;

    boolean previous() throws XQException;

    boolean relative(int itempos) throws XQException;

    void writeSequence(java.io.OutputStream os, java.util.Properties props) throws XQException;

    void writeSequence(java.io.Writer ow, java.util.Properties props) throws XQException;

    void writeSequenceToResult(javax.xml.transform.Result result) throws XQException;

    void writeSequenceToSAX(org.xml.sax.ContentHandler saxhdlr) throws XQException;
}

//
// This interface definition is transcribed from the Public Draft Specification (version 0.9)
// of the XQuery API for Java (XQJ) 1.0 Specification, available at
// http://jcp.org/aboutJava/communityprocess/pr/jsr225/index.html
//
// Copyright 2003, 2006, 2007 Oracle. All rights reserved.
// For licensing conditions, see the above specification
//
