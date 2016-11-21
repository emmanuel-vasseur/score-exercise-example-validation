job('Build-exercise') {
    scm {
        git('https://github.com/2nis6mon/score-project.git')
    }
//    triggers {
//        scm('H/15 * * * *')
//    }
    steps {
        maven('clean install -Dmaven.test.failure.ignore')
    }
}

job('Build-implementation') {
    scm {
        git('https://github.com/2nis6mon/score-project.git')
    }
    steps {
        maven {
			rootPOM('score-exercise-example-impl/pom.xml')
			goals('clean install')
			properties('maven.test.skip': true)
			//localRepository(javaposse.jobdsl.dsl.helpers.LocalRepositoryLocation.LOCAL_TO_WORKSPACE)
		}
    }
}

job('Build-validation') {
    scm {
        git('https://github.com/2nis6mon/score-project.git')
    }
    wrappers {
		timeout {
			absolute(2)
			failBuild()
			writeDescription('Build failed due to a long/stuck build time')
		}
	}
    steps {
        maven {
			rootPOM('score-exercise-example-validation/pom.xml')
			goals('clean test')
			properties('maven.test.failure.ignore': true)
		}
    }
}
