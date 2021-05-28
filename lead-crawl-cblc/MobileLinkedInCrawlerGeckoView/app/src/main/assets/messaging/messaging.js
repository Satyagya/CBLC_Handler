
             const m = document.documentElement.innerHTML;
             let message = {type: "WPAManifest", manifest: m};
             browser.runtime.sendNativeMessage("browser", message);