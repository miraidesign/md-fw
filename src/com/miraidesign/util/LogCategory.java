//------------------------------------------------------------------------
//    LogCategory.java
//                 log4J ヘルパー
//                 Copyright (c) Mirai Design 2010 All Rights Reserved.
//------------------------------------------------------------------------
//      
//------------------------------------------------------------------------
//
//
package com.miraidesign.util;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootCategory;

/**
 *  LogCategory 
 *  
 *  @version 0.5 
 *  @author Toru Ishioka
 *  @since  JDK1.1
**/

public class LogCategory {
    protected boolean debug = false;         // デバッグ表示
    private /*static*/ String FQCN = Category.class.getName();

    // for 1.1.3
    //private  Category cat;
    public Hierarchy hierarchy;  // for 1.2.8 rev.2
    
    // for 1.2.8
    private  Logger logger;
    public Logger getLogger() { return logger; }
    public LoggerRepository getLoggerRepository() { return logger.getLoggerRepository();}

    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------
    public  LogCategory(String name) {
        FQCN = name;    //LogCategory.class.getName();

        // for 1.1.3
        //hierarchy = new Hierarchy(new RootCategory(Priority.DEBUG));
        //cat = hierarchy.getInstance(name);
         
        // for 1.2.8
        //logger = Logger.getLogger(name);
        //logger.setLevel(Level.DEBUG);
        
        // for 1.2.8 rev.2
        hierarchy = new Hierarchy(new RootCategory(Level.DEBUG));
        logger = hierarchy.getLogger(name);
        
    }
    
    //---------------------------------------------------------
    // method
    //---------------------------------------------------------
    public void log(Priority priority, Object message) {
        // for 1.1.3
        //if(cat.getHierarchy().isDisabled(priority.toInt())) {
        //    return;
        //}
        //if(priority.isGreaterOrEqual(cat.getChainedPriority())) {
        //    cat.callAppenders(new LoggingEvent(FQCN, cat, priority, message, null));
        //}
        // for 1.2.8
        if (logger.getLoggerRepository().isDisabled(priority.toInt())) {
            return;
        }
        if (priority.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.callAppenders(new LoggingEvent(FQCN, logger, priority, message, null));
        }
    }
    //---------------------------------------------------------

    public void fatal(Object message) {
        // for 1.1.3
        //if(cat.getHierarchy().isDisabled(Priority.FATAL_INT)) {
        //    return;
        //}
        //if(Priority.FATAL.isGreaterOrEqual(cat.getChainedPriority())) {
        //    cat.callAppenders(new LoggingEvent(FQCN, cat, Priority.FATAL, message, null));
        //}
        // for 1.2.8
        if (logger.getLoggerRepository().isDisabled(Priority.FATAL_INT)) {
            return;
        }
        if (Priority.FATAL.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.callAppenders(new LoggingEvent(FQCN, logger, Priority.FATAL, message, null));
        }
    }

    public void error(Object message) {
        // for 1.1.3
        //if(cat.getHierarchy().isDisabled(Priority.ERROR_INT)) {
        //    return;
        //}
        //if(Priority.ERROR.isGreaterOrEqual(cat.getChainedPriority())) {
        //    cat.callAppenders(new LoggingEvent(FQCN, cat, Priority.ERROR, message, null));
        //}
        // for 1.2.8
        if (logger.getLoggerRepository().isDisabled(Priority.ERROR_INT)) {
            return;
        }
        if (Priority.ERROR.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.callAppenders(new LoggingEvent(FQCN, logger, Priority.ERROR, message, null));
        }
    }
    
    public void warn(Object message) {
        // for 1.1.3
        //if(cat.getHierarchy().isDisabled(Priority.WARN_INT)) {
        //    return;
        //}
        //if(Priority.WARN.isGreaterOrEqual(cat.getChainedPriority())) {
        //    cat.callAppenders(new LoggingEvent(FQCN, cat, Priority.WARN, message, null));
        //}
        // for 1.2.8
        if (logger.getLoggerRepository().isDisabled(Priority.WARN_INT)) {
            return;
        }
        if (Priority.WARN.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.callAppenders(new LoggingEvent(FQCN, logger, Priority.WARN, message, null));
        }
    }
    public void info(Object message) {
        // for 1.1.3
        //if(cat.getHierarchy().isDisabled(Priority.INFO_INT)) {
        //    return;
        //}
        //if(Priority.INFO.isGreaterOrEqual(cat.getChainedPriority())) {
        //    cat.callAppenders(new LoggingEvent(FQCN, cat, Priority.INFO, message, null));
        //}
        // for 1.2.8
        if (logger.getLoggerRepository().isDisabled(Priority.INFO_INT)) {
            return;
        }
        if(Priority.INFO.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.callAppenders(new LoggingEvent(FQCN, logger, Priority.INFO, message, null));
        }
    }
    public void debug(Object message) {
        // for 1.1.3
        //if(cat.getHierarchy().isDisabled(Priority.DEBUG_INT)) {
        //    return;
        //}
        //if(Priority.DEBUG.isGreaterOrEqual(cat.getChainedPriority())) {
        //    cat.callAppenders(new LoggingEvent(FQCN, cat, Priority.DEBUG, message, null));
        //}
        // for 1.2.8
        if (logger.getLoggerRepository().isDisabled(Priority.DEBUG_INT)) {
            return;
        }
        if(Priority.DEBUG.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.callAppenders(new LoggingEvent(FQCN, logger, Priority.DEBUG, message, null));
        }
    }

}
//
// [end of LogCategory.java]
//

