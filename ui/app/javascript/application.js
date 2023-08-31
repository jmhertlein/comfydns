// Entry point for the build script in your package.json
toggle_display_none = (id) => {
    const e = document.getElementById(id)
    if(e.classList.contains("display-none")) {
      e.classList.remove("display-none")
    } else {
      e.classList.add("display-none")
    }
    
  }