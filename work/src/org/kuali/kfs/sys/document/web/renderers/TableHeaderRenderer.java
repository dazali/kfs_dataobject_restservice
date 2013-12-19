/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.sys.document.web.renderers;


/**
 * Renders a table cell as a header
 */
public class TableHeaderRenderer extends TableCellRenderer {

    /**
     * Returns "th" instead of "td"
     * @see org.kuali.kfs.sys.document.web.renderers.TableCellRenderer#getTagName()
     */
    @Override
    protected String getTagName() {
        return "th";
    }

    /**
     * Header cells shouldn't vertically align towards the top of the cell
     * @see org.kuali.kfs.sys.document.web.renderers.TableCellRenderer#verticallyAlignTowardsTop()
     */
    @Override
    protected boolean verticallyAlignTowardsTop() {
        return false;
    }
    
}
