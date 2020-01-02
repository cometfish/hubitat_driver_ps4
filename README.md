# PlayStation 4 driver

Hubitat Driver for detecting status of your PlayStation 4, and waking it up from standby.

## Power status check

1. Add `ps4.groovy` to your Hubitat as a new Driver (under `Drivers Code`)
2. Add a new device for your PlayStation 4 to your Hubitat, set device Type to your User driver of 'PlayStation 4'
3. Assign your PS4 a static IP, and enter the IP into the device's `IP Address` setting in Hubitat.
4. Set the Poll Interval if you want the status to be refreshed every X minutes
5. Pressing Refresh on the device in Hubitat should now load the current switch state (on/off).

## Wake from Standby

To be able to wake the PS4 from standby mode, you need to get a User Credential from linking a device to your PlayStation.

The PlayStation verifies the wake request by the source device's MAC address, so you can only make this work by using your Hubitat's MAC address to make the initial device link. We can't make the initial device link from the Hubitat, because it requires binding to a specific port for listening, so the workaround is to copy the Hubitat's MAC address and make the initial device link with [PS4-Waker](https://github.com/dhleong/ps4-waker). Then any later requests from Hubitat will work.

Here's the instructions on how to get the User Credential - this is a once-off process:

* You will need:
    * a PlayStation
    * a computer with VirtualBox installed on it
    * a mobile device (for the PS4 Second Screen app)
* Find the MAC address of your Hubitat (either through your router's client list, or look underneath the Hubitat box itself)
* Safely shutdown your Hubitat (so you don't get two MAC addresses on the same network)
* Create new VirtualBox machine, give it a name
* Select Linux as the operating system, leave everything else as default
* Download the latest Debian ISO image (netinst version is fine)
* Before starting the new machine, click Settings
* Under Storage, set the CD drive to point to your Debian ISO
* Under Network, set Attached To to Bridged Adapter, then click Advanced, and set the MAC address to the same MAC address as your Hubitat
* Save, then Start your machine
* Install Debian with default settings
    * Mostly just keep clicking Continue
    * Set the machine hostname to start with something like 'hubitat' - the first 6 letters of the name will be shown on the PlayStation when you list mobile devices
    * Set a password for the root account and a user, and remember them
    * Confirm 'write changes to disk' at the end of the Partitioning section
    * Select the existing hard drive for the GRUB bootloader (not Manual)
* Once Debian has installed and rebooted, login with your password and start a Terminal session (in the graphical interface, click Activities, then type Terminal in the search box to find it)
* Switch to the root user: `su root`
* Install npm: `sudo apt install npm`
* Install the ps4-waker node package globally: `npm install ps4-waker -g`
* Install the official PS4 Second Screen app on your mobile phone, and login to your PlayStation account
* On your PlayStation, go to Settings, Mobile App Connection Settings, Add Device
* Run ps4-waker on your virtual machine
* On your mobile phone, in the PS4 Second Screen app, select the 'PS4 Waker' device
* ps4-waker will now ask for a PIN code - enter the PIN code displayed on the PlayStation
* Done (you can close the PS4 Second Screen app on your phone now)
* On the virtual machine, type: `cat ~/.ps4-wake.credentials.json`
* Copy down the value for `"user-credential"`
* Shutdown the virtual machine
* Turn your Hubitat back on
* Paste the User Credential code into your PS4 device's settings, and click Save
* Test it out - if it's worked successfully, you can delete the virtual machine (and the mobile phone app) as it was only needed to get the correct User Credential.

References:  
* https://psdevwiki.com/ps4/Online_Connections  
* https://github.com/dhleong/ps4-waker
* https://github.com/charliechancc/WebWOL
