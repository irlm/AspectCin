/* 
 * Copyright (c) 2004-2005 SLF4J.ORG
 * Copyright (c) 2004-2005 QOS.ch
 *
 * All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, and/or sell copies of  the Software, and to permit persons
 * to whom  the Software is furnished  to do so, provided  that the above
 * copyright notice(s) and this permission notice appear in all copies of
 * the  Software and  that both  the above  copyright notice(s)  and this
 * permission notice appear in supporting documentation.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR  A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * OF  THIRD PARTY  RIGHTS. IN  NO EVENT  SHALL THE  COPYRIGHT  HOLDER OR
 * HOLDERS  INCLUDED IN  THIS  NOTICE BE  LIABLE  FOR ANY  CLAIM, OR  ANY
 * SPECIAL INDIRECT  OR CONSEQUENTIAL DAMAGES, OR  ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS  OF USE, DATA OR PROFITS, WHETHER  IN AN ACTION OF
 * CONTRACT, NEGLIGENCE  OR OTHER TORTIOUS  ACTION, ARISING OUT OF  OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * Except as  contained in  this notice, the  name of a  copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use
 * or other dealings in this Software without prior written authorization
 * of the copyright holder.
 *
 * Note: package renamed to create private version. Based on SL4J 1.0RC5 
 */

package glassbox.util.org.sl4j;

import glassbox.util.org.sl4j.ILoggerFactory;
import glassbox.util.org.sl4j.Logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;

/**
 * Log4jLoggerFactory is an implementation of {@link ILoggerFactory}
 * returning the appropriate named {@link Log4jLoggerAdapter} instance.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class Log4jLoggerFactory implements ILoggerFactory {

  // key: name (String), value: a Log4jLoggerAdapter;
  Map loggerMap;

  public Log4jLoggerFactory() {
    loggerMap = new HashMap();
  }

  /* (non-Javadoc)
   * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
   */
  public Logger getLogger(String name) {
    Logger slf4jLogger = (Logger) loggerMap.get(name);
    if (slf4jLogger == null) {
      org.apache.log4j.Logger logger = LogManager.getLogger(name);
      slf4jLogger = new Log4jLoggerAdapter(logger);
     loggerMap.put(name, slf4jLogger);
    }
    return slf4jLogger;
  }
}
