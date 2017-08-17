/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package RegressionTests;

import org.junit.Before;
import org.junit.runner.RunWith;

import javax.xml.transform.Source;

@RunWith(AzureJenkinsSuite.class)
public class SomeIT extends RegresssionChecker {
    public SomeIT(Source source) {
        super(source);
    }

    @Before
    public void setup() {

    }
}
