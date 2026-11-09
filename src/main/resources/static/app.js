
async function checkKitchen() {
    const status = document.getElementById("api-status");

    try {
        const response = await fetch("/api/v1/menu");

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const menu = await response.json();

        status.textContent =
            `🍕 Kitchen online! ${menu.sizes.length} sizes and ${menu.toppings.length} toppings available.`;

        buildPizzaForm(menu);

    } catch (error) {
        status.textContent = "🔥 The kitchen is warming up. Please try again shortly.";
        console.error("PizzaOps API:", error);
    }
}

function buildPizzaForm(menu) {
    const section = document.querySelector("main section");

    const form = document.createElement("form");
    form.id = "pizza-form";

    const title = document.createElement("h3");
    title.textContent = "Build Your Pizza 🍕";
    form.appendChild(title);

    const fields = [
        ["Size", "sizes"],
        ["Crust", "bases"],
        ["Sauce", "sauces"],
        ["Cheese", "cheeses"]
    ];

    for (const [labelText, key] of fields) {
        const label = document.createElement("label");
        label.textContent = labelText;

        const select = document.createElement("select");
        select.name = key;

        for (const item of menu[key]) {
            const option = document.createElement("option");
            option.value = item.code;
            option.textContent = item.label;
            select.appendChild(option);
        }

        label.appendChild(select);
        form.appendChild(label);
    }

    const toppingsTitle = document.createElement("h4");
    toppingsTitle.textContent = "Choose Your Toppings";
    form.appendChild(toppingsTitle);

    const toppingsContainer = document.createElement("div");
    toppingsContainer.id = "toppings-container";

    for (const topping of menu.toppings) {
        const label = document.createElement("label");
        const checkbox = document.createElement("input");

        checkbox.type = "checkbox";
        checkbox.name = "toppings";
        checkbox.value = topping.code;

        label.appendChild(checkbox);
        label.appendChild(
            document.createTextNode(" " + topping.label)
        );

        toppingsContainer.appendChild(label);
    }

    form.appendChild(toppingsContainer);

    const summary = document.createElement("p");
    summary.id = "pizza-summary";
    form.appendChild(summary);

    function updateSummary() {
        const selected = form.querySelectorAll(
            'input[name="toppings"]:checked'
        ).length;

        const extras = Math.max(0, selected - 4);

        summary.textContent =
            `${selected} toppings selected · ` +
            `${extras} extra toppings · ` +
            `Additional topping charge: £${extras}`;
    }

    
    
form.addEventListener("change", updateSummary);

const button = document.createElement("button");
button.type = "submit";
button.id = "place-order";
button.textContent = "🍕 Place Order";
form.appendChild(button);

const feedback = document.createElement("p");
feedback.id = "order-feedback";
feedback.setAttribute("role", "status");
form.appendChild(feedback);

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const data = new FormData(form);

    const order = {
        size: data.get("sizes"),
        base: data.get("bases"),
        sauce: data.get("sauces"),
        cheese: data.get("cheeses"),
        toppings: data.getAll("toppings")
    };

    button.disabled = true;
    button.textContent = "Preparing your order...";
    feedback.textContent = "";

    try {
        const response = await fetch("/api/v1/orders", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(order)
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(
                result.message || result.error || "Order failed"
            );
        }

        feedback.textContent =
            `✅ Order #${result.id} created successfully!`;

    } catch (error) {
        feedback.textContent =
            `❌ ${error.message}`;
    } finally {
        button.disabled = false;
        button.textContent = "🍕 Place Order";
    }
});

updateSummary();
section.appendChild(form);


}

checkKitchen();
