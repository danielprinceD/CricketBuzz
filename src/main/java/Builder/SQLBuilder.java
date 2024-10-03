package Builder;

public class SQLBuilder {
	
	private SQL sql;
	private boolean hasWhere = false;
	
	public SQLBuilder(SQL sql) {
		this.sql = sql;
	}
	
	public SQLBuilder addString(String name ,String value) {
		
		if(value == null)
			return this;
		if(hasWhere)
			this.sql.sqlString.append(" AND ");
		if(!hasWhere)
		{
			this.sql.sqlString.append(" WHERE ");
			hasWhere = true;
		}
		
		this.sql.sqlString.append(" " + name + " LIKE '%" + value + "%' ");
		
		return this;
	}
	
	public SQLBuilder addInteger(String name ,Integer value) {
			
			if(value == null)
				return this;
			if(hasWhere)
				this.sql.sqlString.append(" AND ");
			if(!hasWhere)
			{
				this.sql.sqlString.append(" WHERE ");
				hasWhere = true;
			}
			
			this.sql.sqlString.append(" " + name + " = " + value + " "); 
			
			return this;
	}
	
	
	public SQL build() {
		return sql;
	}
	
	
	
}
