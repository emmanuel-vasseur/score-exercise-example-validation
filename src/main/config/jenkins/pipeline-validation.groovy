node {
	stage("Clone $team repository") {
		git "https://github.com/emmanuel-vasseur/score-exercise-$team-impl.git"
	}
	stage('Build implementation') {
		timeout(2) {
			maven 'clean cobertura:cobertura', ['maven.test.failure.ignore': true, 'cobertura.report.format': 'xml']
		}
		maven 'verify', ['maven.test.skip': true, 'jar.finalName': "battlecode-2016-$team-impl"]
	}
	def buildMetrics
	stage('Publish implementation') {
		buildMetrics = loadCoverageAndTestsResult(pwd())
		maven 'install:install-file', [file: "target/battlecode-2016-$team-impl.jar",
				groupId: 'org.vasseur', artifactId: "battlecode-2016-$team-impl",
				version: '0.1-SNAPSHOT', pomFile: 'pom.xml']
	}
	stage('Clone validation repository') {
		deleteDir()
		git 'https://github.com/emmanuel-vasseur/score-exercise-example-validation.git'
	}
	stage('Validation implementation') {
		timeout(2) {
			maven 'clean test', computeBonusMalus(buildMetrics) + ['maven.test.failure.ignore': true,
					team: team, 'score.rest-api.url': 'http://localhost:8080']
		}
	}
}

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
    properties.collect{ key, value -> "\"-D$key=$value\"" }.join(' ')
}

@NonCPS
def loadCoverageAndTestsResult(workspace) {
	def results = [:]
	def parser = new XmlSlurper(false, true, true)
	parser.setFeature('http://apache.org/xml/features/nonvalidating/load-external-dtd', false)
	parser.setFeature('http://xml.org/sax/features/namespaces', false)

	def coverageFile = new File(workspace, 'target/site/cobertura/coverage.xml')
	def coverageContent = parser.parse(coverageFile)
	results += [lineCoverage: Double.parseDouble(coverageContent.attributes().'line-rate')]
	results += [branchCoverage: Double.parseDouble(coverageContent.attributes().'branch-rate')]
	results += [complexity: Double.parseDouble(coverageContent.attributes().complexity)]

	def surefireDirectory = new File(workspace, 'target/surefire-reports')
	def numberOfTests = 0
	def numberOfFailures = 0
	def numberOfSkippedTests = 0
	surefireDirectory.eachFileMatch(~/.*\.xml/) { testResultfile ->
		def testResultContent = parser.parse(testResultfile)
		numberOfTests += Integer.parseInt(testResultContent.attributes().tests)
		numberOfFailures += Integer.parseInt(testResultContent.attributes().failures)
		numberOfFailures += Integer.parseInt(testResultContent.attributes().errors)
		numberOfSkippedTests += Integer.parseInt(testResultContent.attributes().skipped)
	}
	results += [numberOfTests: numberOfTests]
	results += [numberOfFailures: numberOfFailures]
	results += [numberOfSkippedTests: numberOfSkippedTests]
	return results
}

@NonCPS
def computeBonusMalus(metrics) {
	def sprintPenality = 200
	def percentOfSuccessTests = (double) (metrics.numberOfTests - metrics.numberOfFailures - metrics.numberOfSkippedTests) / (double) metrics.numberOfTests
	def penalities = sprintPenality + metrics.numberOfFailures * 20 + metrics.numberOfSkippedTests * 10
	// ratio entre 0.5 et 1.5, défini par la couverture du code pondéré du pourcentage de succès des tests
	def ratio = 0.5 + (percentOfSuccessTests * metrics.branchCoverage)
	// On double la mise si le ratio est positif. Ratio final compris entre 0.5 et 2
	if(ratio > 1) {
		ratio += ratio - 1
	}
	return [bonusMalus: -penalities, scoreCoefficient: ratio]
}
