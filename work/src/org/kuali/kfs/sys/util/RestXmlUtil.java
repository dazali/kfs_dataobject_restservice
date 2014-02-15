/*
 * Copyright 2014 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.sys.util;

import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

public class RestXmlUtil {

    /**
     *
     * This method converts a map into xml in the format of <key>value</key>
     *
     * @param boName
     * @param o
     * @return
     */
    public static String toXML(String boName, Object o) {
        XStream xStream = getXStream(boName);

        return xStream.toXML(o);
    }

    /**
     *
     * This method converts the xml back to map.
     *
     * @param boName
     * @param xml
     * @return
     */
    public static Object fromXML(String boName, String xml) {
        XStream xStream = getXStream(boName);

        return xStream.fromXML(xml);
    }

    protected static XStream getXStream(String boName) {
        XStream xStream = new XStream();
        xStream.registerConverter(new MapEntryConverter());
        xStream.alias(List.class.getName(), List.class);
        xStream.alias(boName, Map.class);

        return xStream;
    }
}
