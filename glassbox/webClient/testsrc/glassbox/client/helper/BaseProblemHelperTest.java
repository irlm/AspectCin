/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.analysis.api.ProblemAnalysis;
import glassbox.analysis.api.SingleCallProblem;
import glassbox.client.helper.problems.BaseProblemHelper;
import glassbox.track.api.CallDescription;

import java.util.*;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class BaseProblemHelperTest extends MockObjectTestCase {

    public void testSingleDatabaseProblem() {
        DbProblemTester tester = makeTester(new ProblemAnalysis[] { makeProblem("url1") });
        assertEquals("url1 database", tester.getDbs());
    }
    
    public void testTwoDifferentDatabaseProblems() {
        DbProblemTester tester = makeTester(new ProblemAnalysis[] { makeProblem("http://strangeDatabase"), makeProblem("jdbc://driverMan") });
        assertEquals("http://strangeDatabase and jdbc://driverMan databases", tester.getDbs());
    }

    public void testTwoSameDatabaseProblems() {
        DbProblemTester tester = makeTester(new ProblemAnalysis[] { makeProblem("xyyz"), makeProblem("xyyz") });
        assertEquals("xyyz database", tester.getDbs());
    }

    public void testManyDifferentDatabaseProblems() {
        DbProblemTester tester = makeTester(new ProblemAnalysis[] { makeProblem("http://strangeDatabase"),
                makeProblem("http://strangeDatabase"), makeProblem(null), makeProblem("jdbc://driverMan") });
        assertEquals("http://strangeDatabase, null and jdbc://driverMan databases", tester.getDbs());
    }

    protected DbProblemTester makeTester(ProblemAnalysis[] problems) {
        List problemList = Arrays.asList(problems);
        ArrayList helperList = new ArrayList(problems.length);
        for (int i=0; i<problems.length; i++) {
            helperList.add(new DbProblemTester(problems[i], problemList));
        }
        return (DbProblemTester)helperList.get(0);
    }
    
    protected ProblemAnalysis makeProblem(String url) {
        Mock mockCall = mock(CallDescription.class);
        mockCall.expects(atLeastOnce()).method("getResourceKey").will(returnValue(url));
        Mock mockProblem = mock(SingleCallProblem.class);
        mockProblem.expects(once()).method("getCall").will(returnValue(mockCall.proxy()));
        
        return (ProblemAnalysis)mockProblem.proxy();
    }
    
    private class DbProblemTester extends BaseProblemHelper {
        public DbProblemTester(ProblemAnalysis problem, List problems) { 
            setProblem(problem);
            setProblemsOfType(problems); 
        }
        
        public String getProblemKey() {
            return null;
        }

        public String getDbs() {
            return getAffectedDatabases();
        }
    }
    
}
