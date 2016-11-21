job('Seed-configuration') {
    scm {
        git('C:/Applications/Workspace/score-exercise-example-validation')
//        git('https://github.com/emmanuel-vasseur/score-exercise-example-validation.git')
    }
    triggers {
        cron('H * * * *')
        scm('H/5 * * * *')
    }
    steps {
		jobDsl {
			targets('src/main/config/jenkins/jobs.groovy')
			removedJobAction('DELETE')
			removedViewAction('DELETE')
			failOnMissingPlugin(true)
			unstableOnDeprecation(true)
		}
	}
}

job('Build-battlecode-framework') {
    scm {
        git('https://github.com/2nis6mon/score-project.git')
    }
    triggers {
        scm('H/5 * * * *')
    }
    steps {
        maven('clean install')
    }
}

job('Build-battlecode-exercise') {
    scm {
        git('C:/Applications/Workspace/score-exercise-example')
//        git('https://github.com/emmanuel-vasseur/score-exercise-example.git')
    }
    triggers {
        scm('H/5 * * * *')
    }
    steps {
        maven('clean install')
    }
}

hudson.model.User.all.findAll{ user ->
	user.id != 'admin' && user.getProperty(hudson.security.HudsonPrivateSecurityRealm.Details) != null
}.each { user ->
	job("Build-battlecode-${user.id}-implementation") {
		scm {
			git("C:/Applications/Workspace/score-exercise-${user.id}-impl")
//			git("https://github.com/emmanuel-vasseur/score-exercise-${user.id}-impl.git")
		}
		steps {
			maven {
				goals('clean verify')
				properties('maven.test.skip': true, 'jar.finalName': "battlecode-2016-${user.id}-impl")
			}
			maven {
				goals('install:install-file')
				properties(file: "target/battlecode-2016-${user.id}-impl.jar", groupId: 'org.vasseur', artifactId: "battlecode-2016-${user.id}-impl", version: '0.1-SNAPSHOT', pomFile: 'pom.xml')
			}
		}
	}

	job("Build-battlecode-${user.id}-validation") {
		scm {
			git('C:/Applications/Workspace/score-exercise-example-validation')
//			git('https://github.com/emmanuel-vasseur/score-exercise-example-validation.git')
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
				goals('clean test')
				properties('maven.test.failure.ignore': true, 'team': user.id)
			}
		}
	}
}
