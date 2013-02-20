public class Journey implements Comparable<Journey> {
	String from_date;
	String to_date;
	String from_time;
	String to_time;
	double price;

	public Journey() {

	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(price);
		b.append(" - von ");
		b.append(from_date);
		b.append(" ");
		b.append(from_time);
		b.append(" bis ");
		b.append(to_date);
		b.append(" ");
		b.append(to_time);
		return b.toString();
	}

	public boolean myEquals(Journey other) {
		return other.price == price && other.from_date.equals(from_date)
				&& other.to_date.equals(to_date)
				&& other.from_time.equals(from_time)
				&& other.to_time.equals(to_time);
	}

	@Override
	public int compareTo(Journey other) {
		return other.price > this.price ? 1 : -1;
	}
}