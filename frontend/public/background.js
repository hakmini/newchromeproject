let popupWindowId = null;

chrome.commands.onCommand.addListener(async (command) => {
    console.log("Command executed:", command);

    if (command !== "toggle-window") return;

    // 이미 창이 열려 있으면 닫기
    if (popupWindowId !== null) {
        try {
            await chrome.windows.remove(popupWindowId);
        } catch (e) {
            console.log("Window already closed");
        }
        popupWindowId = null;
        return;
    }

    // 새 창 생성
    const win = await chrome.windows.create({
        url: "index.html",
        type: "popup",
        width: 380,
        height: 570,
        focused: true
    });

    popupWindowId = win.id;
});
