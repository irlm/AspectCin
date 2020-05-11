/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.ProblemAnalysis;

import java.util.List;

public interface ProblemHelper {

     public String getProblemKey();
    
     public String getProblemTypeName(); 
    
     public String getProblemTypeIcon(); 
     
     public String getProblemTypeInfo();
    
     public String getProblemSummary();
     
     public String getProblemDescription();
     
     public List getCommonSolutions();
     
     public String getCommonSolutionTitle();
     
     public StackTraceElement[] getStackTrace(); 
     
     public List getRelatedURLs();
     
     public String getProblemDetailPanel();
     
     public List getEvents();
     
     public ProblemAnalysis getProblem();

     public void setProblem(ProblemAnalysis problem);
     
     public boolean isSymptom();
     
     public boolean isVisible();
}
