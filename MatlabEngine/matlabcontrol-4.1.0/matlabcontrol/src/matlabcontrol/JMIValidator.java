/*******************************************************************************
 * Copyright or © or Copr. Institut des Sciences du Mouvement 
 * (CNRS & Aix Marseille Université)
 * 
 * The DOCoMETER Software must be used with a real time data acquisition 
 * system marketed by ADwin (ADwin Pro and Gold, I and II) or an Arduino 
 * Uno. This software, created within the Institute of Movement Sciences, 
 * has been developed to facilitate their use by a "neophyte" public in the 
 * fields of industrial computing and electronics.  Students, researchers or 
 * engineers can configure this acquisition system in the best possible 
 * conditions so that it best meets their experimental needs. 
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 * 
 * Contributors:
 *  - Frank Buloup - frank.buloup@univ-amu.fr - initial API and implementation [25/03/2020]
 ******************************************************************************/
package matlabcontrol;

/*
 * Copyright (c) 2013, Joshua Kaplan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *  - Neither the name of matlabcontrol nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;

/**
 * Validates that the methods used by {@link JMIWrapper} are present in the current Java Virtual Machine, which should
 * always be MATLAB's JVM when this class is used. This is done because {@code jmi.jar} is entirely undocumented and
 * could change in any future release without notice. If that occurred it could result in a number of exceptions that
 * could be insufficiently informative to resolve the issue. This class throws detailed exceptions when an expected
 * method (or class the method belongs to) is not found.
 * 
 * @since 4.0.0
 * 
 * @author <a href="mailto:nonother@gmail.com">Joshua Kaplan</a>
 */
class JMIValidator
{
    private JMIValidator() { }
    
    /**
     * Checks that all of the methods matlabcontrol uses are present. If they are all present then nothing will happen.
     * If not, then an informative exception is thrown.
     * 
     * @throws MatlabConnectionException 
     */
    static void validateJMIMethods() throws MatlabConnectionException
    {
        //Class: com.mathworks.jmi.Matlab
        Class<?> matlabClass = getAndCheckClass("com.mathworks.jmi.Matlab");
            
        //Method: public static Object mtFevalConsoleOutput(String, Object[], int) throws Exception
        checkMethod(matlabClass, Object.class, "mtFevalConsoleOutput",
                new Class<?>[] { String.class, Object[].class, int.class},
                new Class<?>[] { Exception.class });

        //Method: public static void whenMatlabIdle(Runnable)
        checkMethod(matlabClass, Void.TYPE, "whenMatlabIdle",
                new Class<?>[] { Runnable.class },
                new Class<?>[0]);
        
        
        //Class: com.mathworks.jmi.NativeMatlab
        Class<?> nativeMatlabClass = getAndCheckClass("com.mathworks.jmi.NativeMatlab");

        //Method: public static boolean nativeIsMatlabThread()
        checkMethod(nativeMatlabClass, boolean.class, "nativeIsMatlabThread",
                new Class<?>[0],
                new Class<?>[0]);
    }
    
    private static Class<?> getAndCheckClass(String className) throws MatlabConnectionException
    {
        try
        {
            return Class.forName(className, false, JMIValidator.class.getClassLoader());
        }
        catch(ClassNotFoundException e)
        {
            throw new MatlabConnectionException("This version of MATLAB is missing a class required by matlabcontrol\n" +
                    "Required: " + className, e);
        }
        //Should not occur: MATLAB by default has no SecurityManager installed and PermissiveSecurityManager permits this
        catch(SecurityException e)
        {
            throw new MatlabConnectionException("Unable to verify if MATLAB has the method required by matlabcontrol", e);
        }
    }
    
    /**
     * Checks that there exists a method named {@code methodName} for class {@code clazz} that is {@code public static},
     * return {@code requiredReturn}, has parameters {@code reqiredParameters} (in the specified order), and throws
     * {@code requiredExceptions}. Exceptions that extend {@link RuntimeException} are ignored.
     * 
     * 
     * @param clazz
     * @param requiredReturn
     * @param methodName
     * @param requiredParameters
     * @param requiredExceptions
     * @throws MatlabConnectionException 
     */
    private static void checkMethod(Class<?> clazz, Class<?> requiredReturn, String methodName,
            Class<?>[] requiredParameters, Class<?>[] requiredExceptions) throws MatlabConnectionException
    {
        try
        {
            Method method = clazz.getDeclaredMethod(methodName, requiredParameters);
            int actualModifiers = method.getModifiers();
            Class<?> actualReturn = method.getReturnType();
            Class<?>[] actualExceptions = method.getExceptionTypes();
            
            //Determine if the exceptions are equivalent
            boolean exceptionsEqual = doExceptionsMatch(requiredExceptions, actualExceptions);

            if(!Modifier.isPublic(actualModifiers) || !Modifier.isStatic(actualModifiers) ||
               !actualReturn.equals(requiredReturn) || !exceptionsEqual)
            {
                String required = buildMethodDescription(clazz, requiredReturn, methodName, requiredParameters, requiredExceptions);
                
                throw new MatlabConnectionException("This version of MATLAB is missing a method required by matlabcontrol\n" +
                        "Required: " + required + "\n" +
                        "Found:    " + method.toString());
            }
        }
        catch(NoSuchMethodException e)
        {
            String required = buildMethodDescription(clazz, requiredReturn, methodName, requiredParameters, requiredExceptions);
                
            throw new MatlabConnectionException("This version of MATLAB is missing a method required by matlabcontrol\n" +
                    "Required: " + required);
        }
    }
    
    /**
     * Determine if the {@link Exception} classes are equivalent, ignoring {@link RuntimeException}s.
     * 
     * @param requiredExceptions
     * @param actualExceptions
     * @return 
     */
    private static boolean doExceptionsMatch(Class<?>[] requiredExceptions, Class<?>[] actualExceptions)
    {
        HashSet<Class<?>> requiredSet = new HashSet<Class<?>>();
        for(Class<?> excClass : requiredExceptions)
        {
            if(!RuntimeException.class.isAssignableFrom(excClass))
            {
                requiredSet.add(excClass);
            }
        }
        
        HashSet<Class<?>> actualSet = new HashSet<Class<?>>();
        for(Class<?> excClass : actualExceptions)
        {
            if(!RuntimeException.class.isAssignableFrom(excClass))
            {
                actualSet.add(excClass);
            }
        }
            
        return requiredSet.equals(actualSet);
            
    }
    
    /**
     * Builds a String representation of the form:
     * {@code
     * public static [requiredReturn] [methodName]([requiredParameters]) throws [requiredExceptions]
     * }
     * If no exceptions are thrown then the "throws" part will not occur.
     * 
     * @param clazz
     * @param requiredReturn
     * @param methodName
     * @param requiredParameters
     * @param requiredExceptions
     * @return 
     */
    private static String buildMethodDescription(Class<?> clazz, Class<?> requiredReturn, String methodName,
            Class<?>[] requiredParameters, Class<?>[] requiredExceptions)
    {
        String paramString = "";
        for(int i = 0; i < requiredParameters.length; i++)
        {
            paramString += requiredParameters[i].getCanonicalName();

            if(i < requiredParameters.length - 1)
            {
                paramString += ",";
            }
        }
        
        String throwsString = "";
        if(requiredExceptions.length > 0)
        {
            throwsString = " throws ";
            for(int i = 0; i < requiredExceptions.length; i++)
            {
                throwsString += requiredExceptions[i].getCanonicalName();
                
                if(i < requiredExceptions.length - 1)
                {
                    throwsString += ",";
                }
            }
        }
        
        String desc = "public static " + requiredReturn.getCanonicalName() + " " +
                clazz.getCanonicalName() + "." + methodName + "(" + paramString + ")" + throwsString;
        
        return desc;
    }
}
