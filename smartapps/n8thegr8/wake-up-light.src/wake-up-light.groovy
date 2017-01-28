/**
 *  Wake-Up Light
 *
 *  Copyright 2017 Nathan Maxfield
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Wake-Up Light",
    namespace: "n8thegr8",
    author: "Nathan Maxfield",
    description: "Wake up to a simulated sunrise.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Turn on this light") {
        input "theBulb", "capability.switch", required: true
    }
    section("When and How Long?") {
    	input "theTime", "time", title: "Time to execute every day"
        input "minutes", "number", required: true, title: "Minutes?"
    }
    section("Temperature") {
        input "startTemp", "number", required: true, title: "Start"
    	input "endTemp", "number", required: true, title: "End"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    schedule(theTime, sunriseStartUp)
}

// called every day at the time specified by the user
def sunriseStartUp() {
    // run some diagnostics.  delete this after
    diagnostics()
    
    // how often should the updates happen in seconds
    state.seconds = 5
    
    // get number of steps
    def numSteps = minutes.toInteger() * (60/state.seconds)
    
    // get the color temp difference
    def colorTempDiff = endTemp - startTemp
    
    // get the steps of change in the color temp
    state.colorTempStep = Math.round(colorTempDiff/numSteps).toInteger()
    // get the steps of change in the brightness
    state.brightnessStep = Math.round(100/numSteps).toInteger()
    
    theBulb.setLevel(0)
    theBulb.setColorTemp(startTemp)
    theBulb.on()
    runIn(state.seconds, sunriseFade)
}

def sunriseFade() {
    log.debug "SunriseFade called at ${new Date()}"
    
    def currentTemp = theBulb.currentColorTemp.toInteger()
    def newTemp = currentTemp + state.colorTempStep
    log.debug "newTemp at ${newTemp}"
    
    def currentLevel = theBulb.currentLevel.toInteger()
    def newLevel = currentLevel + state.brightnessStep
    log.debug "newLevel at ${newLevel}"
    
    theBulb.setLevel(newLevel)
    theBulb.setColorTemp(newTemp)
    
    if (newLevel < 100) {
    	log.debug "running again"
        runIn(state.seconds, sunriseFade)
    }else{
    	log.debug "end"
    }
}

def diagnostics() {
	log.debug "Diagnotics called at ${new Date()}"
    
    def theAtts = theBulb.supportedAttributes
    theAtts.each {att ->
        log.debug "Supported Attribute: ${att.name}"
    }
}

