function copyURLtoClipboard() {
    navigator.clipboard.writeText(window.location.href);
    alert("The URL has been copied to the clipboard.");
}
