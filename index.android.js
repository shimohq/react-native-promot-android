import { NativeModules } from 'react-native';
import processColor from 'react-native/Libraries/StyleSheet/processColor';

const PromptAndroid = NativeModules.PromptAndroid;

export type PromptType = $Enum<{
    /**
     * Default alert with no inputs
     */
        'default': string,
    /**
     * Plain text input alert
     */
        'plain-text': string,
    /**
     * Secure text input alert
     */
        'secure-text': string,
    /**
     * Numeric input alert
     */
        'numeric': string,
    /**
     * Email address input alert
     */
        'email-address': string,
    /**
     * Phone pad input alert
     */
        'phone-pad': string,
}>;

export type PromptStyle = $Enum<{
    /**
     * Default alert dialog style
     */
    'default': string,
    /**
     * Shimo alert dialog style
     */
    'shimo': string,
    /**
     * Custom input style
     */
    'cust': string
}>;

export type PromptAction =
  | 'dismissedAction'
  | 'positiveAction'
  | 'negativeAction'
  | 'neutralAction';

type Options = {
    disableFullscreenUI?: boolean;
    cancelable?: boolean;
    type?: PromptType;
    defaultValue?: string;
    style?: PromptStyle;
    placeholder?: string;
    placeholderColor?: string;
    highlightColor?: string;
    color?: string;
    buttonColor?: string;
    onAny?: PromptAction => void,
    onDismiss: () => void
};

/**
 * Array or buttons
 * @typedef {Array} ButtonsArray
 * @property {string=} text Button label
 * @property {Function=} onPress Callback function when button pressed
 */
type ButtonsArray = Array<{
    /**
     * Button label
     */
    text?: string,
    /**
     * Callback function when button pressed
     */
    onPress?: () => void,
}>;

prompt.dismissedAction = 'dismissedAction';
prompt.positiveAction = 'positiveAction';
prompt.negativeAction = 'negativeAction';
prompt.neutralAction = 'neutralAction';
export default function prompt(
    title: ?string,
    message?: ?string,
    callbackOrButtons?: ?((text: string) => void) | ButtonsArray,
    options?: Options
): void {
    const defaultButtons = [
      {
        text: 'Cancel',
      },
      {
        text: 'OK',
        onPress: callbackOrButtons
      }
    ];

    let buttons = typeof callbackOrButtons === 'function'
      ? defaultButtons
      : callbackOrButtons;

    let config = {
        title: title || '',
        message: message || '',
    };

    if (options) {
        config = {
            ...config,
            highlightColor: options.highlightColor ? processColor(options.highlightColor) : options.highlightColor,
            placeholderColor: options.placeholderColor ? processColor(options.placeholderColor) : options.placeholderColor,
            color: options.color ? processColor(options.color) : options.color,
            disableFullscreenUI: options.disableFullscreenUI === true,
            cancelable: options.cancelable !== false,
            type: options.type || 'default',
            style: options.style || 'default',
            defaultValue: options.defaultValue || '',
            placeholder: options.placeholder || null,
            buttonColor: options.buttonColor ? processColor(options.buttonColor) : options.buttonColor
        };
    }
    // At most three buttons (neutral, negative, positive). Ignore rest.
    // The text 'OK' should be probably localized. iOS Alert does that in native.
    const validButtons: Buttons = buttons ? buttons.slice(0, 3) : [{text: 'OK'}];
    const buttonPositive = validButtons.pop();
    const buttonNegative = validButtons.pop();
    const buttonNeutral = validButtons.pop();

    if (buttonNeutral) {
        config = {...config, buttonNeutral: buttonNeutral.text || '' };
    }
    if (buttonNegative) {
        config = {...config, buttonNegative: buttonNegative.text || '' };
    }
    if (buttonPositive) {
        config = {
            ...config,
            buttonPositive: buttonPositive.text || ''
        };
    }

    PromptAndroid.promptWithArgs(
        config,
        (action, buttonKey, input) => {
            if (action === PromptAndroid.dismissed) {
                options.onDismiss && options.onDismiss();
            } else if (action === PromptAndroid.buttonClicked) {
                switch (buttonKey) {
                    case PromptAndroid.buttonNeutral:
                            buttonNeutral.onPress && buttonNeutral.onPress(input);
                        break;
                    case PromptAndroid.buttonNegative:
                            buttonNegative.onPress && buttonNegative.onPress();
                        break;
                    case PromptAndroid.buttonPositive:
                            buttonPositive.onPress && buttonPositive.onPress(input);
                        break;
                    // no default
                }
            }

            if (options.onAny) {
                let actionText;
                if (action === PromptAndroid.buttonClicked) {
                    switch (buttonKey) {
                        case PromptAndroid.buttonNeutral: actionText = prompt.neutralAction; break;
                        case PromptAndroid.buttonPositive: actionText = prompt.positiveAction; break;
                        case PromptAndroid.buttonNegative: actionText = prompt.negativeAction; break;
                    }
                } else if (action === PromptAndroid.dismissed) {
                    actionText = prompt.dismissedAction;
                }

                options.onAny(actionText);
            }
        }
    );
}
