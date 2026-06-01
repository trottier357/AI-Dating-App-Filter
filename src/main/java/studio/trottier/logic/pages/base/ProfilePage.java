package studio.trottier.logic.pages.base;

import ai.djl.util.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * I used Claude for some of the helpers because IDC
 */
public abstract class ProfilePage implements Assessible{
    private static final Duration ELEMENT_TIMEOUT = Duration.ofSeconds(10);

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public ProfilePage(WebDriver driver){
        this.driver = driver;
        this.wait = new WebDriverWait(driver, ELEMENT_TIMEOUT);
    }

    // --- shared wait / lookup helpers ---------------------------------------

    /** Blocks until an element with the given id is present in the DOM, then returns it. */
    protected WebElement waitForId(String id) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
    }

    /** Blocks until an element matching the given CSS selector is present, then returns it. */
    protected WebElement waitForCss(String css) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
    }

    /**
     * Finds the front-most element matching the given locator. Useful for sites that stack
     * cards in the DOM (e.g. swipe UIs) where {@code isDisplayed()} returns true for every
     * stacked element and ids are duplicated across cards.
     *
     * <p>Strategy: ask the browser which of the candidates is actually painted on top at the
     * <em>viewport center</em> (where the active card lives), not at each candidate's own
     * center — back cards are often translated off-screen but still report a non-zero
     * bounding box at their own position, which would fool a per-element hit-test. We also
     * skip candidates whose card is visually hidden, {@code aria-hidden}, {@code inert},
     * or non-interactive ({@code pointer-events: none} / {@code visibility: hidden} /
     * {@code display: none} / zero opacity) anywhere up the tree.
     *
     * @return the front-most match, or {@code null} if none of the matches is actually visible.
     */
    protected WebElement findFrontMost(By locator) {
        List<WebElement> matches = driver.findElements(locator);
        if (matches.isEmpty()) return null;

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Returns the index into the candidates array of the front-most truly-visible match,
        // or -1 if none qualifies.
        final String script =
                "const els = arguments[0];" +
                "function hiddenByAncestor(node){" +
                "  for (let n = node; n && n.nodeType === 1; n = n.parentElement){" +
                "    if (n.hasAttribute('inert')) return true;" +
                "    if (n.getAttribute('aria-hidden') === 'true') return true;" +
                "    const s = getComputedStyle(n);" +
                "    if (s.display === 'none' || s.visibility === 'hidden' || s.visibility === 'collapse') return true;" +
                "    if (parseFloat(s.opacity) === 0) return true;" +
                "    if (s.pointerEvents === 'none') return true;" +
                "  }" +
                "  return false;" +
                "}" +
                "const vw = window.innerWidth || document.documentElement.clientWidth;" +
                "const vh = window.innerHeight || document.documentElement.clientHeight;" +
                "const cx = vw / 2, cy = vh / 2;" +
                "const stack = document.elementsFromPoint(cx, cy) || [];" +
                "let best = -1, bestDepth = Infinity;" +
                "for (let i = 0; i < els.length; i++){" +
                "  const el = els[i];" +
                "  if (!el || hiddenByAncestor(el)) continue;" +
                "  const r = el.getBoundingClientRect();" +
                "  if (r.width === 0 || r.height === 0) continue;" +
                // Prefer the candidate whose subtree is hit at the viewport center,
                // and pick the shallowest (front-most) one in the paint stack.
                "  for (let d = 0; d < stack.length; d++){" +
                "    const hit = stack[d];" +
                "    if (hit === el || el.contains(hit) || hit.contains(el)){" +
                "      if (d < bestDepth){ bestDepth = d; best = i; }" +
                "      break;" +
                "    }" +
                "  }" +
                "}" +
                "return best;";

        Object raw;
        try {
            raw = js.executeScript(script, matches);
        } catch (org.openqa.selenium.StaleElementReferenceException stale) {
            // The deck mutated while we were inspecting it (e.g. mid swipe-out). The caller
            // is typically polling, so signal "no front-most right now" and let them retry.
            return null;
        }
        int idx = (raw instanceof Number) ? ((Number) raw).intValue() : -1;
        if (idx >= 0 && idx < matches.size()) {
            try {
                WebElement candidate = matches.get(idx);
                // Touch the element so a stale reference fails here (caller polls) instead of later.
                candidate.isEnabled();
                return candidate;
            } catch (org.openqa.selenium.StaleElementReferenceException stale) {
                return null;
            }
        }
        return null;
    }

    protected String readFrontTextById(String id) {
        waitForId(id);
        WebElement front = findFrontMost(By.id(id));
        return textOf(front);
    }

    protected void clickFrontById(String id) {
        waitForId(id);
        WebElement front = findFrontMost(By.id(id));
        if (front == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "No visible front-most element with id=" + id);
        }
        try {
            front.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", front);
        }
    }

    /**
     * Like {@link #readFrontTextById} but matches by CSS, and reads a descendant of the
     * front-most match via a relative XPath. Use this when the stacked card has an
     * unstable id (e.g. {@code profileColumn-<uid>}) so you must match by id prefix.
     *
     * @param cardCss        CSS that locates each stacked card (e.g. {@code [id^="profileColumn-"]}).
     * @param relativeXPath  XPath relative to the front-most card. Must start with {@code ./}.
     */
    protected String readFrontTextByCss(String cardCss, String relativeXPath) {
        waitForCss(cardCss);
        WebElement front = findFrontMost(By.cssSelector(cardCss));
        if (front == null) return "";
        try{
            WebElement target = front.findElement(By.xpath(relativeXPath));
            return textOf(target);
        }catch(Exception e){
            return "";
        }
    }

    /**
     * Returns the trimmed text of every direct child of the element located by
     * {@code parentRelativeXPath} (resolved relative to the front-most card matched by
     * {@code cardCss}). Use this for tag/chip strips where each child element is one item.
     * Empty/blank entries are skipped.
     */
    protected List<String> readFrontChildTextsByCss(String cardCss, String parentRelativeXPath) {
        java.util.List<String> out = new java.util.ArrayList<>();
        waitForCss(cardCss);
        WebElement front = findFrontMost(By.cssSelector(cardCss));
        if (front == null) return out;
        try {
            WebElement parent = front.findElement(By.xpath(parentRelativeXPath));
            for (WebElement child : parent.findElements(By.xpath("./*"))) {
                String t = textOf(child);
                if (!t.isEmpty()) out.add(t);
            }
        } catch (Exception ignored) {
            // Parent not present on this card (some profiles omit the tag strip); return empty.
        }
        return out;
    }

    /**
     * Returns the text of the section that follows a heading/label matching {@code labelText}
     * (case-insensitive, trimmed) inside the front-most card matched by {@code cardCss}.
     *
     * <p>This is resilient to layout shuffling between profiles: instead of positional
     * xpath like {@code ./div[4]/div/...}, we ask the browser to find the element whose
     * visible text equals the label, then walk to the nearest following block of prose.
     *
     * @return the section text, or {@code ""} if the label or its content can't be found.
     */
    protected String readFrontSectionByLabel(String cardCss, String labelText) {
        waitForCss(cardCss);
        WebElement front = findFrontMost(By.cssSelector(cardCss));
        if (front == null) return "";

        JavascriptExecutor js = (JavascriptExecutor) driver;
        final String script =
                "const root = arguments[0];" +
                "const want = (arguments[1] || '').trim().toLowerCase();" +
                "if (!root || !want) return '';" +
                // Find every descendant whose own (direct) text equals the label.
                "const all = root.querySelectorAll('*');" +
                "let label = null;" +
                "for (const el of all){" +
                "  let own = '';" +
                "  for (const n of el.childNodes){ if (n.nodeType === 3) own += n.nodeValue; }" +
                "  if (own.trim().toLowerCase() === want){ label = el; break; }" +
                "}" +
                // Fallback: any element whose full textContent equals the label.
                "if (!label){" +
                "  for (const el of all){" +
                "    if ((el.textContent || '').trim().toLowerCase() === want){ label = el; break; }" +
                "  }" +
                "}" +
                "if (!label) return '';" +
                // Walk up to a container that has a sibling holding the section body.
                "let cur = label;" +
                "while (cur && cur !== root){" +
                "  let sib = cur.nextElementSibling;" +
                "  while (sib){" +
                "    const t = (sib.innerText || sib.textContent || '').trim();" +
                "    if (t.length > 0 && t.toLowerCase() !== want) return t;" +
                "    sib = sib.nextElementSibling;" +
                "  }" +
                "  cur = cur.parentElement;" +
                "}" +
                "return '';";

        Object raw = js.executeScript(script, front, labelText);
        return raw == null ? "" : raw.toString().trim();
    }

    /**
     * Returns the longest visible prose block inside the front-most card matched by
     * {@code cardCss}. A "prose block" is a leaf-ish element with non-trivial text
     * (multiple words, not a button/heading). Useful for the bio, which is almost
     * always the largest text chunk on a Boo card and doesn't have a stable label.
     */
    protected String readFrontLongestProse(String cardCss) {
        waitForCss(cardCss);
        WebElement front = findFrontMost(By.cssSelector(cardCss));
        if (front == null) return "";

        JavascriptExecutor js = (JavascriptExecutor) driver;
        final String script =
                "const root = arguments[0];" +
                "if (!root) return '';" +
                "const skip = new Set(['BUTTON','A','H1','H2','H3','H4','H5','H6','LABEL','SCRIPT','STYLE','SVG','PATH']);" +
                "let best = '', bestLen = 0;" +
                "const walker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT);" +
                "let el = walker.currentNode;" +
                "while (el){" +
                "  if (!skip.has(el.tagName)){" +
                "    const s = getComputedStyle(el);" +
                "    if (s.display !== 'none' && s.visibility !== 'hidden'){" +
                // Sum direct text-node children only, so we don't double-count nested prose.
                "      let own = '';" +
                "      for (const n of el.childNodes){ if (n.nodeType === 3) own += n.nodeValue; }" +
                "      own = own.trim();" +
                "      if (own.length > bestLen && own.includes(' ')){" +
                "        bestLen = own.length; best = own;" +
                "      }" +
                "    }" +
                "  }" +
                "  el = walker.nextNode();" +
                "}" +
                "return best;";

        Object raw = js.executeScript(script, front);
        return raw == null ? "" : raw.toString().trim();
    }

    /**
     * Returns the {@code id} attribute of the front-most element matching {@code cardCss},
     * or {@code null} if no visible match exists. Useful as a "card identity" snapshot
     * for waiting on swipe-deck transitions.
     */
    protected String frontIdByCss(String cardCss) {
        WebElement front = findFrontMost(By.cssSelector(cardCss));
        if (front == null) return null;
        try {
            return front.getAttribute("id");
        } catch (org.openqa.selenium.StaleElementReferenceException stale) {
            // Card was ripped out of the DOM between the front-most lookup and this read
            // (common during Boo's swipe-out animation). Treat as "no stable front yet".
            return null;
        }
    }

    /**
     * Blocks until the front-most element matching {@code cardCss} has an id different
     * from {@code previousId} (and is non-null). Use this after an action that triggers
     * a swipe-deck advance, so subsequent reads don't observe the outgoing card while
     * its exit animation is still in flight.
     */
    protected void waitForFrontCardChange(String cardCss, String previousId) {
        wait.ignoring(org.openqa.selenium.StaleElementReferenceException.class)
            .until(d -> {
                String current = frontIdByCss(cardCss);
                return current != null && !current.equals(previousId);
            });
    }

    /**
     * Returns (prompt, answer) pairs found inside the front-most card matched by
     * {@code cardCss}. Matches structurally rather than by positional xpath, because the
     * prompts block's index shifts based on how many photo tiles the profile has.
     *
     * <p>The match pattern: any {@code <p>} whose immediately-following sibling is a
     * {@code <div>} containing a {@code <p>}. The first {@code <p>} is the prompt, the
     * nested {@code <p>} is the answer. This mirrors Boo's prompt markup:
     * <pre>
     *   &lt;div&gt;
     *     &lt;p&gt;Prompt question&lt;/p&gt;
     *     &lt;div&gt;&lt;p&gt;Answer&lt;/p&gt;&lt;/div&gt;
     *   &lt;/div&gt;
     * </pre>
     */
    protected java.util.List<Pair<String, String>> readFrontPromptPairs(String cardCss) {
        java.util.List<Pair<String, String>> out = new java.util.ArrayList<>();
        waitForCss(cardCss);
        WebElement front = findFrontMost(By.cssSelector(cardCss));
        if (front == null) return out;

        // XPath: a <p> directly inside a <div> whose very next sibling <div> contains a <p>.
        // Restricting to `div/p` (rather than `.//p`) avoids matching prompt text that's
        // itself nested inside the answer wrapper, which would produce duplicates.
        List<WebElement> promptPs;
        try {
            promptPs = front.findElements(
                    By.xpath(".//div/p[following-sibling::div[1]/p]"));
        } catch (Exception e) {
            return out;
        }

        for (WebElement promptP : promptPs) {
            try {
                String question = textOf(promptP);
                WebElement answerP = promptP.findElement(
                        By.xpath("./following-sibling::div[1]//p"));
                String answer = textOf(answerP);
                if (!question.isEmpty() && !answer.isEmpty()) {
                    out.add(new Pair<>(question, answer));
                }
            } catch (Exception ignored) {
                // Skip any node that doesn't actually have the answer p (defensive).
            }
        }
        return out;
    }

    private static String textOf(WebElement el) {
        if (el == null) return "";
        String text = el.getText();
        if (text != null && !text.isBlank()) return text.trim();
        String dom = el.getDomProperty("textContent");
        return dom == null ? "" : dom.trim();
    }

    // --- profile data abstraction -------------------------------------------

    public abstract String getId();
    public abstract String getName();
    public abstract String getUrl();

    public abstract String getBio();
    public abstract List<String> getTags();
    public abstract List<Pair<String, String>> getPrompts();
    public abstract List<String> getPhotoUrls();

    public abstract void reject();

    // Override

    @Override
    public String getInfo(){
        String result = "";

        String bio = getBio();
        if(!bio.isBlank()){
            result += "Bio: " + bio + "\n\n";
        }

        List<String> tags = getTags();
        if(!tags.isEmpty()){
            result += "Interests & Hobbies: ";
            boolean first = true;

            for(String tag : getTags()){
                if(!first){
                    result += ", ";
                }
                result += tag;
                first = false;
            }

            result += "\n\n";
        }

        List<Pair<String, String>> prompts = getPrompts();
        if(!prompts.isEmpty()){
            result += "Prompts:\n";
            for(Pair<String, String> prompt : prompts){
                result += " - " + prompt.getKey() + ": " + prompt.getValue() + "\n";
            }
        }

        return result;
    }

    protected List<Pair<String, String>> preventDuplication(List<Pair<String, String>> pairs){
        List<Pair<String, String>> result = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for(Pair<String, String> pair : pairs){
            if(!seenKeys.contains(pair.getKey())){
                result.add(pair);
                seenKeys.add(pair.getKey());
            }
        }

        return result;

    }
}
