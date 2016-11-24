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
    triggers {
        cron '*/5 * * * *'
    }
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
		definition {
			cps {
				sandbox true
				script """\
node {
	stage('Clone $team repository') {
		git 'https://github.com/emmanuel-vasseur/score-exercise-$team-impl.git'
	}
	stage('Build implementation') {
		maven 'clean verify', ['maven.test.skip': true, 'jar.finalName': 'battlecode-2016-$team-impl']
	}
	stage('Publish implementation') {
		maven 'install:install-file', [file: 'target/battlecode-2016-$team-impl.jar',
				groupId: 'org.vasseur', artifactId: 'battlecode-2016-$team-impl',
				version: '0.1-SNAPSHOT', pomFile: 'pom.xml']
	}
	stage('Clone validation repository') {
		deleteDir()
		git 'https://github.com/emmanuel-vasseur/score-exercise-example-validation.git'
	}
	stage('Validation implementation') {
		timeout(2) {
			maven 'clean test', ['maven.test.failure.ignore': true, team: '$team']
		}
	}
}
""" + '''\
def Closure maven(task, properties) {
	def mavenCommandLine = "mvn -B $task ${joinProperties(properties)}"
	if(isUnix()) {
		sh mavenCommandLine
	} else {
		bat mavenCommandLine
	}
}
@NonCPS
def joinProperties(properties) {
    properties.collect{ key, value -> "\\\"-D$key=$value\\\"" }.join(' ')
}
'''
			}
		}
	}
}
