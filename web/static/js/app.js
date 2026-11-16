'use strict';

/**
 * Проверка форм на стороне клиента и подтверждение необратимых действий.
 * Те же правила проверяются на сервере в классе Validator.
 */
class FormValidator {
    #form;

    constructor(form) {
        this.#form = form;
        form.addEventListener('submit', (event) => {
            if (!this.validate()) {
                event.preventDefault();
            }
        });
        form.querySelectorAll('input, select').forEach((field) => {
            field.addEventListener('change', () => this.#check(field));
        });
    }

    validate() {
        const fields = [...this.#form.querySelectorAll('input:not([type=hidden]), select')];
        const invalid = fields.filter((field) => !this.#check(field));
        invalid[0]?.focus();
        return invalid.length === 0;
    }

    #check(field) {
        const message = this.#messageFor(field);
        const output = this.#form.querySelector(`.field-error[data-for="${field.name}"]`);
        if (output) {
            output.textContent = message;
        }
        field.toggleAttribute('aria-invalid', message !== '');
        if (message !== '') {
            field.setAttribute('aria-invalid', 'true');
        }
        return message === '';
    }

    #messageFor(field) {
        const { validity } = field;
        if (validity.valueMissing) {
            return 'Заполните это поле';
        }
        if (validity.badInput || validity.typeMismatch) {
            return field.type === 'email' ? 'Укажите корректный адрес электронной почты' : 'Некорректное значение';
        }
        if (validity.patternMismatch) {
            return field.title || 'Значение не соответствует формату';
        }
        if (validity.rangeUnderflow) {
            return `Значение должно быть не меньше ${field.min}`;
        }
        if (validity.rangeOverflow) {
            return `Значение должно быть не больше ${field.max}`;
        }
        if (validity.stepMismatch) {
            return field.step === '1' ? 'Укажите целое число' : `Точность не более ${field.step}`;
        }
        if (validity.tooLong) {
            return `Не длиннее ${field.maxLength} символов`;
        }
        if (field.dataset.noFuture && field.value && new Date(field.value) > new Date()) {
            return 'Дата не может быть в будущем';
        }
        if (field.dataset.match && field.value !== this.#form.elements[field.dataset.match]?.value) {
            return 'Значения не совпадают';
        }
        return '';
    }
}

document.querySelectorAll('form[data-validate]').forEach((form) => new FormValidator(form));

document.querySelectorAll('form[data-confirm]').forEach((form) => {
    form.addEventListener('submit', (event) => {
        if (!window.confirm(form.dataset.confirm)) {
            event.preventDefault();
        }
    });
});
