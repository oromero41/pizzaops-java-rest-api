// PizzaOps — single-screen ordering studio
// Product catalog and order persistence come from the existing Spring Boot API.
// This is a demonstration app; there is no payment functionality.

const INCLUDED_TOPPINGS = 4;
const EXTRA_TOPPING_PRICE_GBP = 1;

function setConnectionState(message, state) {
  const status = document.getElementById("api-status");
  if (!status) return;
  status.className = `connection-status is-${state}`;
  status.replaceChildren();
  const dot = document.createElement("i");
  dot.setAttribute("aria-hidden", "true");
  status.append(dot, document.createTextNode(message));
}

async function loadMenu() {
  const loading = document.getElementById("builder-loading");
  const retry = document.getElementById("retry-menu");
  if (retry) retry.hidden = true;
  setConnectionState("Connecting kitchen...", "connecting");
  try {
    const response = await fetch("/api/v1/menu", { cache: "no-store" });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const menu = await response.json();
    for (const key of ["sizes", "bases", "sauces", "cheeses", "toppings"]) {
      if (!Array.isArray(menu[key]) || menu[key].length === 0) {
        throw new Error(`Invalid catalog: ${key}`);
      }
    }
    renderStudio(menu);
    setConnectionState("Kitchen online", "online");
  } catch (error) {
    console.error("PizzaOps catalog:", error);
    setConnectionState("Kitchen unavailable", "offline");
    if (loading) {
      loading.hidden = false;
      loading.querySelector("strong").textContent = "Our kitchen is taking a little longer.";
      loading.querySelector("p").textContent = "Check the connection or try again.";
    }
    if (retry) retry.hidden = false;
  }
}

function shortLabel(code, original) {
  const shortcuts = {
    THIN: "Thin", THICK: "Thick", STUFFED: "Stuffed",
    BARBEQUE: "BBQ", LOW_FAT: "Low-fat"
  };
  return shortcuts[code] || original;
}

function renderStudio(menu) {
  const builder = document.getElementById("builder");
  if (!builder) return;
  builder.replaceChildren();

  const layout = document.createElement("div");
  layout.className = "studio-layout";
  const form = document.createElement("form");
  form.id = "pizza-form";
  form.className = "editor-card";
  form.setAttribute("aria-label", "Customize your pizza");

  const editorTop = document.createElement("div");
  editorTop.className = "editor-top";
  editorTop.innerHTML = `
    <div><span class="step-indicator">01 / THE GOOD STUFF</span><h2>Build your pizza</h2></div>
    <button type="reset" class="reset-button" aria-label="Reset pizza selections">↺ Reset</button>
  `;
  form.appendChild(editorTop);

  const choiceArea = document.createElement("div");
  choiceArea.className = "choices-grid";
  const fields = [
    { key: "sizes", name: "Size", kicker: "How hungry are you?" },
    { key: "bases", name: "Crust", kicker: "The foundation" },
    { key: "sauces", name: "Sauce", kicker: "Bring the flavour" },
    { key: "cheeses", name: "Cheese", kicker: "The good melt" }
  ];
  for (const { key, name, kicker } of fields) {
    const fieldset = document.createElement("fieldset");
    fieldset.className = `choice-group choice-${key}`;
    const legend = document.createElement("legend");
    legend.textContent = name;
    const hint = document.createElement("span");
    hint.className = "field-hint";
    hint.textContent = kicker;
    const options = document.createElement("div");
    options.className = "radio-options";
    for (const [idx, item] of menu[key].entries()) {
      const label = document.createElement("label");
      label.className = "choice-pill";
      label.title = item.label;
      const radio = document.createElement("input");
      radio.type = "radio";
      radio.name = key;
      radio.value = item.code;
      radio.required = true;
      if (idx === 0) radio.checked = true;
      const copy = document.createElement("span");
      copy.textContent = shortLabel(item.code, item.label);
      label.append(radio, copy);
      options.append(label);
    }
    fieldset.append(legend, hint, options);
    choiceArea.append(fieldset);
  }
  form.append(choiceArea);

  const toppingArea = document.createElement("section");
  toppingArea.className = "topping-area";
  toppingArea.innerHTML = `
    <div class="toppings-heading"><div><span class="step-indicator">02 / MAKE IT SPECIAL</span><h2>Pick your toppings</h2></div>
    <span class="free-note">FIRST 4 FREE · +£1 EACH EXTRA</span></div>
  `;
  const groups = new Map();
  for (const topping of menu.toppings) {
    const group = topping.category === "Meat" ? "Meat lovers" :
      topping.category === "Veg" ? "Garden fresh" : "Others";
    if (!groups.has(group)) groups.set(group, []);
    groups.get(group).push(topping);
  }
  for (const [groupName, items] of groups) {
    const section = document.createElement("div");
    section.className = "topping-group";
    const label = document.createElement("div");
    label.className = "topping-group-label";
    label.textContent = groupName;
    const grid = document.createElement("div");
    grid.className = "topping-grid";
    for (const item of items) {
      const itemLabel = document.createElement("label");
      itemLabel.className = "topping-choice";
      const input = document.createElement("input");
      input.type = "checkbox";
      input.name = "toppings";
      input.value = item.code;
      const name = document.createElement("span");
      name.textContent = item.label;
      const check = document.createElement("span");
      check.className = "topping-check";
      check.setAttribute("aria-hidden", "true");
      check.textContent = "+";
      itemLabel.append(input, check, name);
      grid.append(itemLabel);
    }
    section.append(label, grid);
    toppingArea.append(section);
  }
  form.append(toppingArea);

  const receipt = document.createElement("aside");
  receipt.className = "receipt-card";
  receipt.setAttribute("aria-label", "Your live order summary");
  receipt.innerHTML = `
    <div class="receipt-header"><span class="receipt-overline">YOUR ORDER / LIVE PREVIEW</span><span class="receipt-badge">DEMO</span></div>
    <h2>One great pizza.<br><em>All yours.</em></h2>
    <div class="receipt-divider"></div>
    <div class="receipt-fields">
      <div><span>SIZE</span><strong id="summary-size">—</strong></div>
      <div><span>CRUST</span><strong id="summary-base">—</strong></div>
      <div><span>SAUCE</span><strong id="summary-sauce">—</strong></div>
      <div><span>CHEESE</span><strong id="summary-cheese">—</strong></div>
    </div>
    <div class="receipt-divider"></div>
    <div class="receipt-topping-title"><strong>YOUR TOPPINGS</strong><span id="summary-toppings-count">0 selected</span></div>
    <div id="summary-toppings" class="receipt-chips"><span class="empty-toppings">Nothing added yet. Make it yours!</span></div>
    <div class="receipt-bottom">
      <div class="receipt-stats"><span id="summary-included">0 of 4 included</span><span id="summary-extras">0 extras</span></div>
      <div class="receipt-charge"><div><span>EXTRA TOPPING CHARGE</span><small>Base pizza price not included</small></div><strong id="summary-charge">£0</strong></div>
      <button form="pizza-form" id="place-order" type="submit" class="order-button">Place demo order <span aria-hidden="true">↗</span></button>
      <div id="order-feedback" class="order-feedback" role="status" aria-live="polite"></div>
      <p class="receipt-disclaimer">A working software demo. No payment or real delivery.</p>
    </div>
  `;
  layout.append(form, receipt);
  builder.append(layout);

  const feedback = receipt.querySelector("#order-feedback");
  const submitButton = receipt.querySelector("#place-order");
  const selectCode = key => form.querySelector(`input[name="${key}"]:checked`)?.value;
  const selectedToppings = () => Array.from(form.querySelectorAll('input[name="toppings"]:checked'), x => x.value);
  const catalogLabel = (key, code) => menu[key].find(x => x.code === code)?.label || code || "—";

  function updateReceipt() {
    for (const [key, id] of [["sizes", "size"], ["bases", "base"], ["sauces", "sauce"], ["cheeses", "cheese"]]) {
      receipt.querySelector(`#summary-${id}`).textContent = catalogLabel(key, selectCode(key));
    }
    const selected = selectedToppings();
    const extra = Math.max(0, selected.length - INCLUDED_TOPPINGS);
    receipt.querySelector("#summary-toppings-count").textContent = `${selected.length} selected`;
    receipt.querySelector("#summary-included").textContent = `${Math.min(selected.length, INCLUDED_TOPPINGS)} of 4 included`;
    receipt.querySelector("#summary-extras").textContent = `${extra} extra${extra === 1 ? "" : "s"}`;
    receipt.querySelector("#summary-charge").textContent = `£${extra * EXTRA_TOPPING_PRICE_GBP}`;
    const chips = receipt.querySelector("#summary-toppings");
    chips.replaceChildren();
    if (selected.length === 0) {
      const hint = document.createElement("span");
      hint.className = "empty-toppings";
      hint.textContent = "Nothing added yet. Make it yours!";
      chips.append(hint);
    } else {
      for (const code of selected) {
        const chip = document.createElement("span");
        chip.className = "receipt-chip";
        chip.textContent = catalogLabel("toppings", code);
        chips.append(chip);
      }
    }
  }
  function clearFeedback() {
    feedback.replaceChildren();
    feedback.className = "order-feedback";
  }

  form.addEventListener("change", () => { updateReceipt(); clearFeedback(); });
  form.addEventListener("reset", () => {
    clearFeedback();
    requestAnimationFrame(updateReceipt);
  });
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const order = {
      size: selectCode("sizes"),
      base: selectCode("bases"),
      sauce: selectCode("sauces"),
      cheese: selectCode("cheeses"),
      toppings: selectedToppings()
    };
    submitButton.disabled = true;
    submitButton.textContent = "Sending order...";
    clearFeedback();
    try {
      const response = await fetch("/api/v1/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(order)
      });
      const result = await response.json().catch(() => null);
      if (!response.ok) throw new Error(result?.message || result?.error || `HTTP ${response.status}`);
      if (result?.id == null) throw new Error("Missing order ID in API response");
      feedback.classList.add("is-success");
      const msg = document.createElement("strong");
      msg.textContent = `✓ Order #${result.id} created`;
      const link = document.createElement("a");
      link.href = `/api/v1/orders/${encodeURIComponent(String(result.id))}`;
      link.target = "_blank";
      link.rel = "noopener noreferrer";
      link.textContent = "View JSON ↗";
      feedback.append(msg, link);
    } catch (error) {
      feedback.classList.add("is-error");
      feedback.textContent = `Order failed: ${error.message}`;
      console.error("PizzaOps order:", error);
    } finally {
      submitButton.disabled = false;
      submitButton.innerHTML = 'Place demo order <span aria-hidden="true">↗</span>';
    }
  });
  updateReceipt();
}

const retry = document.getElementById("retry-menu");
if (retry) retry.addEventListener("click", loadMenu);
loadMenu();
