/**
 *  Switch Sends Custom Commands
 *
 *  Copyright 2016 Patrick DiLeonardo
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
    name: "Switch Sends Custom Commands",
    namespace: "pdileonardo",
    author: "Patrick DiLeonardo",
    description: "Send a custom command to a target device when a trigger switch turns on or off",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	page(name: "getPref")
}
	
def getPref() {    
    dynamicPage(name: "getPref", install:true, uninstall: true) {
    	section("Choose a trigger switch...") {
			input "triggerSwitch", "capability.switch", title: "Switch", options: switchCommandsStr,  multiple: false, required: true
    	}
        section("Choose a target device...") {
			def capabilities = ['Momentary',  'Switch',  'Thermostat',  'Acceleration Sensor',  'Actuator',  'Alarm',  'Battery',  'Beacon',  'Button',  'Carbon Dioxide Measurement',  
            	'Carbon Monoxide Detector',  'Color Control',  'Color Temperature',  'Configuration',  'Consumable',  'Contact Sensor',  
                'Door Control',  'Energy Meter',  'Estimated Time of Arrival',  'Garage Door Control',  'Illuminance Measurement',  
                'Image Capture',  'Indicator',  'Lock',  'Media Controller',  'Motion Sensor',  'Music Player',  
                'Notification',  'Occupancy',  'pH Measurement',  'Polling',  'Power',  'Power Meter',  'Presence Sensor', 'Refresh',  
                'Relative Humidity Measurement',  'Relay Switch',  'Sensor',  'Shock Sensor',  'Signal Strength',  'Sleep Sensor',  'Smoke Detector',  
                'Sound Pressure Level',  'Sound Sensor',  'Speech Recognition',  'Speech Synthesis',  'Step Sensor',  'Switch Level',  
                'Tamper Alert',  'Temperature Measurement',  'Thermostat Cooling Setpoint',  'Thermostat Fan Mode',  
                'Thermostat Heating Setpoint',  'Thermostat Mode',  'Thermostat Operating State',  'Thermostat Setpoint',  'Three Axis', 
                'Timed Session',  'Tone',  'Touch Sensor',  'Ultraviolet Index',  'Valve',  'Voltage Measurement',  'Water Sensor',  'Window Shade' ]
        	input "targetCapability", "enum", title: "Select Capability", options: capabilities, submitOnChange: true, required: true
            if (targetCapability) {
// Convert human readable capability type to what's needed for input statement parameter
            	def capabilityType = targetCapability
                capabilityType = capabilityType.replaceAll("\\s","")
                capabilityType = capabilityType.replaceFirst(capabilityType[0], capabilityType[0].toLowerCase())
                capabilityType = "capability.${capabilityType}"
                log.debug "Capability: ${capabilityType}"
	            input "targetDevice", "$capabilityType", title: "Select Device", submitOnChange: true, multiple: false
            }
        }
//	See if selected target device has the selected capability - don't show commands section if not
		def targetDeviceHasCapability = false
        if (targetDevice && targetCapability) {
            targetDevice.capabilities.each {
                if (it.name == targetCapability) {
                    targetDeviceHasCapability = true
                }
            }
		}
		if (targetDevice && targetDeviceHasCapability) {
            def switchCommands = targetDevice.supportedCommands
			log.debug "$switchCommands"
// Get human readable name for device's command methods
//	and, ensure that onCmd and offCmd are present in supported commands - don't show respective parameters section if not
            def targetDeviceHasOnCmd = false
            def targetDeviceHasOffCmd = false
            def switchCommandsStr = []
            switchCommands.each { 
                switchCommandsStr << it.name
                if (onCmd && (it.name == onCmd)) {
                	targetDeviceHasOnCmd = true
				}
                if (offCmd && (it.name == offCmd)) {
                	targetDeviceHasOffCmd = true
				}
			}

            def parmTypes = ['none','string','integer','decimal']
            switchCommandsStr.sort()
            section("Issue this command on target device...") {
                input "onCmd", "enum", title: "When trigger switch turned on", options: switchCommandsStr, submitOnChange: true, required: false
				if (onCmd && targetDeviceHasOnCmd) {
					input "onType", "enum", title: "On command parameter type", options: parmTypes, defaultValue: 'none', submitOnChange: true, required: true
                    switch (onType) {
                        case 'string':
                           input "onVal", "text", title: "On string parameter", required: true
                           break
                        case 'integer':
                           input "onVal", "number", title: "On integer parameter", required: true
                           break
                        case 'decimal':
                           input "onVal", "decimal", title: "On decimal parameter", required: true
                           break
                        default:
						   settings.remove('onVal')
                           break
					}
				} else { 
                	settings.remove('onType')
                	settings.remove('onVal')
				}
                input "offCmd", "enum", title: "When trigger switch turned off", options: switchCommandsStr, submitOnChange: true, required: false
				if (offCmd && targetDeviceHasOffCmd) {
					input "offType", "enum", title: "Off command parameter type", options: parmTypes, defaultValue: 'none', submitOnChange: true, required: true
                    switch (offType) {
                        case 'string':
                           input "offVal", "text", title: "Off string parameter", required: true
                           break
                        case 'integer':
                           input "offVal", "number", title: "Off integer parameter", required: true
                           break
                        case 'decimal':
                           input "offVal", "decimal", title: "Off decimal parameter", required: true
                           break
                        default:
						   settings.remove('offVal')
                           break
					}
				} else { 
                	settings.remove('offType')
                	settings.remove('offVal')
                }
             }		
		}
		section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false)
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
		}
    }
}

page(name: "pageAbout", title: "About ${textAppName()}") {
        section {
            paragraph "${textVersion()}\n${textCopyright()}\n\n${textLicense()}\n"
        }
        section("Instructions") {
            paragraph textHelp()
        }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	subscribe(triggerSwitch, "switch", "switchHandler")
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(triggerSwitch, "switch", "switchHandler")
}

def switchHandler(evt) {
	log.debug "Switch event: $evt.value"
	if (onCmd && (evt.value == "on")) {
		if (!onType || (onType == 'none') ) {
			log.debug "Running on command: $onCmd with no parameter"
        	targetDevice."$onCmd"()
		} else {
			log.debug "Running on command: $onCmd with parameter: $onVal"
        	targetDevice."$onCmd"(onVal)
		}
	} 
    else if (offCmd && (evt.value == "off")) {
		if (!offType || (offType == 'none') ) {
        	targetDevice."$offCmd"()
			log.debug "Running off command: $offCmd with no parameter"
		} else {
			log.debug "Running off command: $offCmd with parameter: $offVal"
        	targetDevice."$offCmd"(offVal)
		}
	}
}



//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Switch Sends Custom Commands"
}	

private def textVersion() {
    def text = "Version 1.1.0 (06/02/2016)"
}

private def textCopyright() {
    def text = "Copyright © 2016 Patrick DiLeonardo"
}

private def textLicense() {
    def text =
		"Licensed under the Apache License, Version 2.0 (the 'License'); "+
		"you may not use this file except in compliance with the License. "+
		"You may obtain a copy of the License at"+
		"\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0"+
		"\n\n"+
		"Unless required by applicable law or agreed to in writing, software "+
		"distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
		"See the License for the specific language governing permissions and "+
		"limitations under the License."
}

private def textHelp() {
	def text =
    	"Send a custom command to a target device when a trigger switch turns on or off.  Has many uses"+
		"Simply choose a switch (Virtual Momentary switches work especially well for this purpose)"+ 
        "then tie the on/off state of the switch to trigger a custom command on a target device."+
        "(target device must have capability of switch)"+
		"Operate the switch manually or tie it to an IFTTT action, or change it with a Smartrule (IOS)" 
}