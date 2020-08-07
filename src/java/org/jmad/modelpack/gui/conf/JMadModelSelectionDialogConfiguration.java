/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.jmad.modelpack.gui.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that only creates the beans for the jmad-modelpack-gui. It expects all the necessary beans
 * already in the context. You can use the {@link JMadModelSelectionDialogStandaloneConfiguration} if you want to
 * have a fully configured, ready-to-use, context.
 */
@Configuration
@ComponentScan(basePackageClasses = { JMadModelSelectionDialogConfiguration.class })
public class JMadModelSelectionDialogConfiguration {

}
