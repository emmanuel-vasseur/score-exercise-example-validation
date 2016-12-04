job('Seed-configuration') {
    scm {
        git 'https://github.com/emmanuel-vasseur/score-exercise-example-validation'
    }
    triggers {
        cron '0 * * * *'
        scm 'H/15 * * * *'
    }
    steps {
		jobDsl {
			targets 'src/main/config/jenkins/jobs.groovy'
			removedJobAction 'DELETE'
			removedViewAction 'DELETE'
			failOnMissingPlugin true
			unstableOnDeprecation true
		}
	}
}

job('Build-battlecode-framework') {
    scm {
        git 'https://github.com/2nis6mon/score-project.git'
    }
    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        maven 'clean install'
    }
}

job('Build-battlecode-exercise') {
    scm {
        git 'https://github.com/emmanuel-vasseur/score-exercise-example.git'
    }
    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        maven 'clean install'
    }
}

def getTeams() {
	hudson.model.User.all.findAll{ team ->
		team.id != 'admin' && team.getProperty(hudson.security.HudsonPrivateSecurityRealm.Details) != null
	}*.id
}

pipelineJob('Run-battlecode-validation') {
	concurrentBuild false
	definition {
		cps {
			sandbox true
			script "${getTeams().collect{ team -> "build job: 'Validate-$team-battlecode-implementation', wait: false" }.join('\n')}"
		}
	}
}

getTeams().each { team ->
	pipelineJob("Validate-$team-battlecode-implementation") {
		concurrentBuild false
		parameters {
			stringParam('team', team)
		}
		definition {
			cpsScm {
				scm {
					git 'https://github.com/emmanuel-vasseur/score-exercise-example-validation'
				}
				scriptPath ('src/main/config/jenkins/pipeline-validation.groovy')
			}
		}
	}
}
