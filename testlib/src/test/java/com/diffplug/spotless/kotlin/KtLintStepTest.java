/*
 * Copyright 2016-2022 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless.kotlin;

import org.junit.jupiter.api.Test;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.ResourceHarness;
import com.diffplug.spotless.SerializableEqualityTester;
import com.diffplug.spotless.StepHarness;
import com.diffplug.spotless.TestProvisioner;

/**
 * This class is the only one that uses jcenter, and it seems to be the only one that
 * causes these problems. The root is still a gradle bug, but in the meantime we don't
 * need to hold up *every* PR with this: https://github.com/gradle/gradle/issues/11752
 */
class KtLintStepTest extends ResourceHarness {
	@Test
	void behavior() throws Exception {
		FormatterStep step = KtLintStep.create(TestProvisioner.mavenCentral());
		StepHarness.forStep(step)
				.testResource("kotlin/ktlint/basic.dirty", "kotlin/ktlint/basic.clean")
				.testResourceException("kotlin/ktlint/unsolvable.dirty", assertion -> {
					assertion.isInstanceOf(AssertionError.class);
					assertion.hasMessage("Error on line: 1, column: 1\n" +
							"Wildcard import");
				});
	}

	@Test
	void worksShyiko() throws Exception {
		FormatterStep step = KtLintStep.create("0.31.0", TestProvisioner.mavenCentral());
		StepHarness.forStep(step)
				.testResource("kotlin/ktlint/basic.dirty", "kotlin/ktlint/basic.clean")
				.testResourceException("kotlin/ktlint/unsolvable.dirty", assertion -> {
					assertion.isInstanceOf(AssertionError.class);
					assertion.hasMessage("Error on line: 1, column: 1\n" +
							"Wildcard import");
				});
	}

	// Regression test to ensure it works on the version it switched to Pinterest (version 0.32.0)
	// but before 0.34.
	// https://github.com/diffplug/spotless/issues/419
	@Test
	void worksPinterestAndPre034() throws Exception {
		FormatterStep step = KtLintStep.create("0.32.0", TestProvisioner.mavenCentral());
		StepHarness.forStep(step)
				.testResource("kotlin/ktlint/basic.dirty", "kotlin/ktlint/basic.clean")
				.testResourceException("kotlin/ktlint/unsolvable.dirty", assertion -> {
					assertion.isInstanceOf(AssertionError.class);
					assertion.hasMessage("Error on line: 1, column: 1\n" +
							"Wildcard import");
				});
	}

	// Regression test to handle alpha and 1.x version numbers
	// https://github.com/diffplug/spotless/issues/668
	@Test
	void worksAlpha1() throws Exception {
		FormatterStep step = KtLintStep.create("0.38.0-alpha01", TestProvisioner.mavenCentral());
		StepHarness.forStep(step)
				.testResource("kotlin/ktlint/basic.dirty", "kotlin/ktlint/basic.clean");
	}

	@Test
	void equality() throws Exception {
		new SerializableEqualityTester() {
			String version = "0.32.0";

			@Override
			protected void setupTest(API api) {
				// same version == same
				api.areDifferentThan();
				// change the version, and it's different
				version = "0.38.0-alpha01";
				api.areDifferentThan();
			}

			@Override
			protected FormatterStep create() {
				String finalVersion = this.version;
				return KtLintStep.create(finalVersion, TestProvisioner.mavenCentral());
			}
		}.testEquals();
	}
}
