import React, { Component } from 'react';
import {request, requestMultiple, PERMISSIONS} from 'react-native-permissions';
import {
  SafeAreaView,
  StyleSheet,
  View,
  Text,
  NativeEventEmitter,
  NativeModules,
  Pressable, 
  Platform
} from 'react-native';

const eventEmitter = new NativeEventEmitter(NativeModules.EventEmitter);
const { ClientManager } = NativeModules;

const styles = StyleSheet.create({
  status: {
    padding: 20,
    alignItems: 'center',
    justifyContent: 'center'
  },
  container: {
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100%'
  },
  button: {
    borderRadius: 8,
    borderColor: 'blue',
    borderWidth: 2,
    padding: 6,
    height: 40,
    width: 200,
    justifyContent: 'center',
    alignItems: 'center'
  },
  buttonText: {
    fontSize: 16,
    color: 'black',
  },
  callState: {
    textAlign: 'center',
    padding: 6,
    width: 200,
    height: 30
  }
})

const token = 'ALICE_JWT';
const number = 'PHONE_NUMBER';

class App extends Component<{}, { status: string, callState: string, button: string, callAction: any }> {
  constructor(props: any) {
    super(props);
    this.state = {
      status: "Unknown",
      callState: "Idle",
      button: "Login",
      callAction: () => ClientManager.login(token)
    };
  }

  componentDidMount() {
    if (Platform.OS === 'ios') {
      request(PERMISSIONS.IOS.MICROPHONE);
    } else if (Platform.OS === 'android') {
      requestMultiple([PERMISSIONS.ANDROID.RECORD_AUDIO, PERMISSIONS.ANDROID.READ_PHONE_STATE]);
    }

    eventEmitter.addListener('onStatusChange', (data) => {
      const status = data.status;
      this.setState({ status: status });

      if (status === 'connected' || status === 'Connected') {
        this.setState({ button: "Call"});
        this.setState({ callAction: () => ClientManager.makeCall(number)});
      }
    });

    eventEmitter.addListener('onCallStateChange', (data) => {
      const state = data.state;
      this.setState({ callState: state });

      if (state == 'On Call') {
        this.setState({ button: "End Call"});
        this.setState({ callAction: () => ClientManager.endCall()});
      } else if (state == 'Idle') {
        this.setState({ button: "Call"});
        this.setState({ callAction: () => ClientManager.makeCall(number)});
      }
    });
  }

  componentWillUnmount() {
    eventEmitter.removeAllListeners('onStatusChange');
    eventEmitter.removeAllListeners('onCallStateChange');
  }

  render() {
    return (
      <SafeAreaView>
        <View style={styles.status}>
          <Text>
            {this.state.status}
          </Text>

          <View style={styles.container}>
            <Text style={styles.callState}>
              Call Status: {this.state.callState}
            </Text>
            <Pressable
              style={styles.button}
              onPress={this.state.callAction}>
              <Text style={styles.buttonText}>{this.state.button}</Text>
            </Pressable>
          </View>
        </View>
      </SafeAreaView>
    );
  }
}

export default App;