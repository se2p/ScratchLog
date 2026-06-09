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

package de.uni_passau.fim.se2.scratchlog;

import de.uni_passau.fim.se2.scratchlog.testing_utils.EntityUtilService;
import de.uni_passau.fim.se2.scratchlog.testing_utils.EventUtilService;
import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.doReturn;

/**
 * Base class for all integration tests in ScratchLog that need to use
 * Spring-instantiated and injected classes.
 *
 * <p>Add frequently needed helper classes and methods here.
 *
 * <p>Also add mocked beans here once instead of in the subclasses. This avoids
 * having to restart the Spring application multiple times during tests (due to
 * the mock bean in the concrete test class, the application context is
 * different and therefore a restart is required).
 * <em>Note</em>: Please only mock beans when really necessary. Usually that
 * should only be necessary when interacting with other external systems
 * (e.g. mail). For most tests, please set up the required entities in the
 * database first and then test against this ‘actual’ data rather than mocking
 * the database and/or ScratchLog service/repository/controller beans.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractScratchLogTest {

    @Autowired
    protected EntityUtilService entityUtilService;

    @Autowired
    protected EventUtilService eventUtilService;

    @MockitoSpyBean
    protected ApplicationProperties applicationProperties;

    @AfterEach
    void resetMocks() {
        Mockito.reset(applicationProperties);
    }

    protected void setMailServer(final boolean useMail) {
        doReturn(useMail).when(applicationProperties).useMail();
    }
}
