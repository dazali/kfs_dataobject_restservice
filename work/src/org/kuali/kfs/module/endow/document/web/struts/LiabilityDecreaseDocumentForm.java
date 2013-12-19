/*
 * Copyright 2010 The Kuali Foundation.
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
package org.kuali.kfs.module.endow.document.web.struts;

import org.kuali.kfs.module.endow.document.LiabilityDecreaseDocument;

public class LiabilityDecreaseDocumentForm extends EndowmentTransactionLinesDocumentFormBase {

    public LiabilityDecreaseDocumentForm() 
    {
        super();
        
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "ELD";
    }

    /**
     * This method gets the asset increase document
     * 
     * @return the AssetIncreaseDocument
     */
    public LiabilityDecreaseDocument getLiabilityDecreaseDocument() {
        return (LiabilityDecreaseDocument) getDocument();
    }
}