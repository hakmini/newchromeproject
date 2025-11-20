chrome.commands.onCommand.addListener((command) => {
    if (command === "toggle-popup") {
        chrome.action.openPopup();
    }
});