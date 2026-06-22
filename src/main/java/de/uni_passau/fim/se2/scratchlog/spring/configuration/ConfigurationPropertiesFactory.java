/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ConfigurationPropertiesFactory {

    /**
     * Automatically provided code embedding model configuration.
     * @return The configuration
     */
    @Bean
    @Profile(Constants.PROFILE_CODE_EMBEDDINGS)
    @ConfigurationProperties(prefix = "code-embeddings")
    public CodeEmbeddingConfiguration codeEmbeddingConfiguration() {
        return new CodeEmbeddingConfiguration();
    }

    /**
     * Automatically provided Whisker configuration.
     * @return The configuration
     */
    @Bean
    @Profile(Constants.PROFILE_WHISKER)
    @ConfigurationProperties(prefix = "whisker")
    public WhiskerConfiguration whiskerConfiguration() {
        return new WhiskerConfiguration();
    }

}
