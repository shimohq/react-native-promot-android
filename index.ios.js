import {
    Alert
} from 'react-native';

export default function prompt(
    title: ?string,
    message?: ?string,
    callbackOrButtons?: ?((text: string) => void) | Object,
    options?: Object
): void {
    Alert.prompt(title, message, callbackOrButtons, options.type, options.defaultValue, options.keyboardType);
};
