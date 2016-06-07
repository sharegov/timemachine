/*******************************************************************************
 * Copyright 2014 Miami-Dade County
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.sharegov.timemachine

import javax.persistence.*


@Entity
@Table(name="CIRM_QRTM_HISTORY")
class HistoryTask {

	@Id
	@SequenceGenerator(name="LOG_ID_SEQ", sequenceName="CIRM_SEQUENCE")
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="LOG_ID_SEQ")
	Long id

		
	@Column(name = "NAME", nullable = false)
	String name

	@Column(name = "GROUP_NAME", nullable = true)
	String group

	
	@Column(name = "SUCCESS", nullable = true)
	Integer success
	
	@Column(name = "MESSAGE", nullable = true)
	String message

	@Column(name = "FIRE_TIME", nullable = false)
	Date fireTime
		
	@Lob
	@Column(name = "TASK", nullable = true)
	String task
	
	@Lob
	@Column(name = "ANSWER", nullable = true)
	String answer
	


	
}
