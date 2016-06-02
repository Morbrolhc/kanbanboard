'use strict';

/**
 * @ngdoc service
 * @name kanbanboardFrontendApp.languageLoader
 * @description
 * # languageLoader
 * Factory in the kanbanboardFrontendApp.
 */
angular.module('kanbanboardFrontendApp')
  .factory('languageLoader', ['$q', '$timeout', function ($q, $timeout) {

    // Public API here
      return function (options) {
        var deferred = $q.defer();
        var translations;
        
        if (options.key === 'en') {
          translations = {
            order: "Order",
            customer: "Customer",
            delivery: "Delivery",
            developers: "Developers",

            members: "Board Members",
            searchTask: "Search Task",
            boardName: "Board Name",

            welcome: "Welcome {{username}}",
            searchBoard: "Search Board",
            allTasks: "Assigned Tasks",
            newBoard: "New Board",

            username: "Username",
            password: "Password",
            register: "Register",
            resetPassword: "Reset Password",
            displayName: "Displayname",
            language: "Language",
            cancel: "Cancel",

            description: "Task description",
            delete: "Delete",
            save: "Save",
            emailOrUsername: "Username or email",
            assignUser: "Assign task",

            oldPassword: "Old Password",
            newPassword: "New Password",
            deleteAccount: "Delete Account",
            passwordrepeat: "Repeat Password",
            
            resetTokenExpired: "Can not reset account. Try resetting account again",
            
            activation: "Activation",
            activardText: "Your users is activated. You will be forwarded to the login page.",
            activationErrorText: "User could not be activated",
            message: "Message",
            
            confirmBoard: "Delete Board",
            questionBoard: "Confirm delete of this board",
            confirmTask: "Delete Task",
            questionTask: "Confirm delete of this board",
            confirmFile: "Delete File",
            questionFile: "Confirm delete of this file",
            confirmUser: "Delete Account",
            questionUser: "Confirm delete of your account",
            
            boardOwnerConfirm: "Info",
            boardOwnerMessage: "The owner of a board can not be deleted."
          };
        } else { //language DE
          translations = {
            order: 'Auftrag',
            customer: 'Auftraggeber',
            delivery: 'Abgabe',
            developers: 'Entwickler',

            members: 'Board Mitglieder',
            searchTask: 'Task Suche',
            boardName: 'Board Name',

            welcome: 'Willkommen',
            searchBoard: 'Boardsuche',
            allTasks: 'Zugewiesene Tasks',
            newBoard: 'Neues Board',

            username: 'Benutzername',
            password: 'Passwort',
            register: 'Registrieren',
            resetPassword: 'Passwort zurücksetzen',
            displayName: 'Anzeigename',
            language: 'Sprache',
            cancel: 'Abbrechen',
            error: 'Fehler',
            
            cardname: 'Taskname',
            description: 'Beschreibung des Tasks',
            delete: 'Löschen',
            save: 'Speichern',
            emailOrUsername: 'Benutzername oder email',
            assignUser: 'Task zuweisen',

            oldPassword: 'Altes Passwort',
            newPassword: 'Neues Passwort',
            deleteAccount: 'Account löschen',
            passwordrepeat: 'Passwort wiederholen',
            
            resetTokenExpired: 'Die Zeit um den Passwortreset durchzuführen ist abgelaufen. Bitte versuchen Sie es erneut unter Login/Reset',
            
            activation: 'Aktivierung',
            activardText: 'Ihr Benutzer ist aktiviert. Sie werden in kürze auf die Loginseite weitergeleitet',
            activationErrorText: 'User konnte nicht aktiviert werden',
            message: 'Nachricht',
            
            confirmBoard: 'Board entfernen',
            questionBoard: 'Dieses Board wirklich entfernen?',
            confirmTask: 'Task entfernen',
            questionTask: 'Diesen Task wirklich entfernen?',
            confirmFile: 'File entfernen',
            questionFile: 'Dieses File wirklich entfernen?',
            confirmUser: 'Account entfernen',
            questionUser: 'Mein Account wirklich entfernen?',
            
            boardOwnerConfirm: 'Info',
            boardOwnerMessage: 'Der Besitzer des Boards kann nicht entfernt werden'
          };
        }  
        $timeout(function () {
          deferred.resolve(translations);
        }, 100);
    
        return deferred.promise;
    };
  }]);
