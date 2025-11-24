chrome.commands.onCommand.addListener(async (command) => {
    if (command === "toggle_popup") {
        const views = chrome.extension.getViews({ type: "popup" });

        if (views.length > 0) {
            // 이미 popup이 열려 있다면 닫기
            chrome.action.setPopup({ popup: "" });

            // popup 창을 닫기 위해 아이콘을 한번 토글
            chrome.action.openPopup().catch(() => {});
            return;
        }

        // popup이 닫혀 있다면 열기
        try {
            await chrome.action.openPopup();
        } catch (e) {
            console.error("Failed to open popup:", e);
        }
    }
});
