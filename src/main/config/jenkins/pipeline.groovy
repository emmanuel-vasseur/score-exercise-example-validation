stage('Clone validation repository') {
	node {
		git 'https://github.com/emmanuel-vasseur/score-exercise-example-validation.git'
		stash name: 'sources', includes: 'pom.xml,src/'
    }
}

stage('Build and validate team implementations') {
    def parallelNodes = [:]
    def teams = getTeams()

    for (int i = 0; i < teams.size(); i++) {
        def team = teams.get(i)
        parallelNodes[team] = validateTeamImplementation(team)
    }

    parallel parallelNodes
}

@NonCPS
def getTeams() {
	hudson.model.User.all.findAll{ user ->
		user.id != 'admin' && user.getProperty(hudson.security.HudsonPrivateSecurityRealm.Details) != null
	}*.id
}

def Closure validateTeamImplementation(String team) {
    return {
        // use a closure for later execution of this node (one per instance)
        node {
			git("https://github.com/emmanuel-vasseur/score-exercise-${team}-impl.git")
			bat "mvn -B clean verify -Dmaven.test.skip=true -Djar.finalName=battlecode-2016-${team}-impl"
			bat "mvn -B install:install-file -Dfile=target/battlecode-2016-${team}-impl.jar -DgroupId=org.vasseur -DartifactId=battlecode-2016-${team}-impl -Dversion=0.1-SNAPSHOT -DpomFile=pom.xml"
        }
        node {
			deleteDir()
			unstash 'sources'
			timeout(2) {
    			bat "mvn -B clean test -Dmaven.test.failure.ignore=true -Dteam=$team"
			}
        }
    }
}
