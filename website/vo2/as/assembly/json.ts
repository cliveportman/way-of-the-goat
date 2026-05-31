// Hand-rolled JSON builder. AssemblyScript has no JSON.stringify; rather
// than pull in a library we encode our known schema directly.

export class JsonBuilder {
  private parts: string[] = [];

  push(s: string): void { this.parts.push(s); }

  toString(): string { return this.parts.join(''); }
}

export function jsonEscape(s: string): string {
  let out = '"';
  for (let i = 0; i < s.length; i++) {
    const c = s.charCodeAt(i);
    if (c == 0x22) out += '\\"';
    else if (c == 0x5C) out += '\\\\';
    else if (c == 0x0A) out += '\\n';
    else if (c == 0x0D) out += '\\r';
    else if (c == 0x09) out += '\\t';
    else if (c < 0x20) {
      out += '\\u00';
      out += (c < 16 ? '0' : '1');
      const lo = c & 0xF;
      out += lo < 10 ? String.fromCharCode(48 + lo) : String.fromCharCode(87 + lo);
    } else {
      out += String.fromCharCode(c);
    }
  }
  out += '"';
  return out;
}

// JSON number — preserves integer formatting when possible so the output
// matches Rust serde, which emits 70 not 70.0 for whole f64 values.
export function jsonNumber(v: f64): string {
  if (isNaN(v) || !isFinite(v)) return 'null';
  if (v == Math.floor(v) && Math.abs(v) < 1e16) {
    return (<i64>v).toString();
  }
  return v.toString();
}

export function jsonNumberOrNull(v: f64): string {
  return isNaN(v) ? 'null' : jsonNumber(v);
}

export function jsonIntOrNull(v: i32): string {
  return v < 0 ? 'null' : v.toString();
}
