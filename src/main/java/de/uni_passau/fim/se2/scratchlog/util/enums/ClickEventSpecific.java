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

package de.uni_passau.fim.se2.scratchlog.util.enums;

/**
 * All possible specific events for a click event.
 */
public enum ClickEventSpecific {

    /**
     * The user clicked on the green flag icon.
     */
    GREENFLAG,

    /**
     * The user clicked on the stop all icon.
     */
    STOPALL,

    /**
     * The user started the test execution.
     */
    START_TESTS,

    /**
     * The user started the next tutorial step.
     */
    NEXT_STEP,

    /**
     * The user opened the help menu for a specific test.
     */
    OPEN_HELP_PAGE,

    /**
     * The user clicked the button to generate a new hint.
     */
    GENERATE_NEW_HINT,

    /**
     * The user clicked on a non-test block.
     */
    STACKCLICK,

    /**
     * The user rewound the execution slider.
     */
    REWIND_EXECUTION_SLIDER_CHANGE,

    /**
     * The user revisited the previous step in the block execution.
     */
    STEP_BACK,

    /**
     * The user jumped over an execution step.
     */
    STEP_OVER,

    /**
     * The user paused the execution of a code block.
     */
    PAUSE_EXECUTION,

    /**
     * The user resumed the execution of a code block.
     */
    RESUME_EXECUTION,

    /**
     * The user deactivated the observation.
     */
    DEACTIVATE_OBSERVATION,

    /**
     * The user activated the observation.
     */
    ACTIVATE_OBSERVATION,

    /**
     * The user closed the debugger.
     */
    CLOSE_DEBUGGER,

    /**
     * The user clicked on the run button of an individual test.
     */
    BBT_RUN_INDIVIDUAL,

    /**
     * The user clicked on the button to run the entire test suite.
     */
    BBT_RUN_ALL,

    /**
     * The user clicked the trash icon to clear any test results from the BBT interface.
     */
    BBT_CLEAR_RESULTS,

    /**
     * The user clicked the icon to toggle the batch tests window.
     */
    BBT_TOGGLE_BATCH_WINDOW,

    /**
     * The user clicked the icon to execute the batch tests on a single project.
     */
    BBT_RUN_BATCH_ON_FILE,

    /**
     * The user clicked the icon to execute the batch tests on all projects.
     */
    BBT_RUN_BATCH_ON_SUITE,

    /**
     * The user uploaded Whisker tests.
     */
    BBT_UPLOAD_WHISKER_TESTS,

    /**
     * The user clicked on a block describing a test.
     */
    BBT_STACKCLICK,

    /**
     * The user clicked on the icon to open LitterBox.
     */
    LB_OPEN,

    /**
    * The user clicked to close LitterBox.
    */
    LB_CLOSE,

    /**
    * The user clicked the Code Quality button in LitterBox.
    */
    LB_CODE_QUALITY,

    /**
    * The user clicked the Check Again! button in LitterBox.
    */
    LB_CHECK_AGAIN,

    /**
    * The user clicked the Bugs button in LitterBox.
    */
    LB_BUGS,

    /**
    * The user clicked the Smells button in LitterBox.
    */
    LB_SMELLS,

    /**
    * The user clicked the Elegant Code button in LitterBox.
    */
    LB_ELEGANT_CODE,

    /**
    * The user clicked the Ask about Code button in LitterBox.
    */
    LB_ASK,

    /**
    * The user clicked the Ask about whole program button in LitterBox.
    */
    LB_ASK_PROGRAM,

    /**
     * The user clicked the Ask about whole program button in LitterBox.
     */
    LB_ASK_SPRITE,

    /**
     * The user clicked the GPT: Fix the issue! button in LitterBox.
     */
    LB_GPT_FIX,

    /**
     * The user clicked the GPT: Explain the issue! button in LitterBox.
     */
    LB_GPT_EXPLAIN,

    /**
     * The user clicked the Revert Fix button in LitterBox.
     */
    LB_REVERT_FIX,

    /**
     * The user clicked the Code Understanding button in LitterBox.
     */
    LB_CODE_UNDERSTANDING,

    /**
     * The user clicked the Code Understanding Check Again! button in LitterBox.
     */
    LB_CODE_UNDERSTANDING_CHECK_AGAIN,

    /**
     * The user clicked the Check Answer! button in LitterBox.
     */
    LB_CHECK_ANSWER,
}
