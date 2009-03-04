//========================================================================
//$Id: AndroidLog.java 40 2008-06-03 07:09:41Z janb.webtide $
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.artcom.y60.infrastructure.dc;

import org.mortbay.log.Logger;

import android.util.Log;

public class AndroidLog implements Logger
{
    public static final String __JETTY_TAG = "Jetty";
    
    
    public AndroidLog()
    {
        this ("org.mortbay.log");
    }
    
    public AndroidLog(String name)
    {     
    }
    
    public void debug(String msg, Throwable th)
    {
        Log.d(__JETTY_TAG, msg, th);
    }

    public void debug(String msg, Object arg0, Object arg1)
    {
        Log.d(__JETTY_TAG, msg);
    }

    public Logger getLogger(String name)
    {
       return new AndroidLog(name);
    }

    public void info(String msg, Object arg0, Object arg1)
    {
        Log.i(__JETTY_TAG, msg);
    }

    public boolean isDebugEnabled()
    {
        return Log.isLoggable(__JETTY_TAG, Log.DEBUG);
    }

    public void setDebugEnabled(boolean enabled)
    {
        //not supported by android logger
    }

    public void warn(String msg, Object arg0, Object arg1)
    {
        Log.w(__JETTY_TAG, msg);
    }

    public void warn(String msg, Throwable th)
    {
        Log.e(__JETTY_TAG, msg, th);
    }

}