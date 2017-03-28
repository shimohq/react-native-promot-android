import {
    AlertIOS
} from 'react-native';

export default function prompt(
    title: ?string,
    message?: ?string,
    callbackOrButtons?: ?((text: string) => void) | Object,
    options?: Object
): void {
    let type = 'plain-text';
    if (options && options.type) {
        type = options.type;
    }

    let defaultValue = '';
    if (options && options.defaultValue) {
        defaultValue = options.defaultValue;
    }

    AlertIOS.prompt(title, message, callbackOrButtons, type, defaultValue);
};
